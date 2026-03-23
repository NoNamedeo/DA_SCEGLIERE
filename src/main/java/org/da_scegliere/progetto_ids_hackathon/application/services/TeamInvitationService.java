package org.da_scegliere.progetto_ids_hackathon.application.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.da_scegliere.progetto_ids_hackathon.application.config.properties.TeamCreationProperties;
import org.da_scegliere.progetto_ids_hackathon.application.ports.events.DomainEventPublisher;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.INotificationRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IUserRepository;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.team.TeamCreationRequestClosedException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.team.TeamCreationRequestNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.team.TeamInvitationAlreadyProcessedException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.team.TeamInvitationNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.user.UserNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.views.team.TeamCreationRequestView;
import org.da_scegliere.progetto_ids_hackathon.application.services.views.team.TeamInvitationView;
import org.da_scegliere.progetto_ids_hackathon.core.entities.notification.TeamInviteNotification;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Team;
import org.da_scegliere.progetto_ids_hackathon.core.entities.user.User;
import org.da_scegliere.progetto_ids_hackathon.core.enums.state.team.TeamCreationRequestStatus;
import org.da_scegliere.progetto_ids_hackathon.core.enums.state.team.TeamInvitationStatus;
import org.da_scegliere.progetto_ids_hackathon.core.events.team.TeamInvitationRejectedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Team-creation workflow based on invitation notifications.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TeamInvitationService {

    private static final int NOTIFICATION_PRIORITY = 3;

    private final INotificationRepository notificationRepository;
    private final IUserRepository userRepository;
    private final TeamService teamService;
    private final TeamCreationProperties teamCreationProperties;
    private final Clock clock;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional
    public TeamCreationRequestView startTeamCreationRequest(UUID creatorId, String teamName, List<UUID> inviteeIds) {
        log.info("Starting team creation request creatorId={} teamName={}", creatorId, teamName);
        requireNonNullId(creatorId, "creatorId");
        requireNonBlank(teamName, "teamName");
        validateInvitees(inviteeIds);

        User creator = getUserOrThrow(creatorId);
        ensureUserHasNoTeam(creator, "Creator already belongs to a team.");

        int minimumRequiredMembers = validateMinimumMembers();
        if (inviteeIds.size() < minimumRequiredMembers - 1) {
            throw new IllegalArgumentException(
                    "At least " + (minimumRequiredMembers - 1) + " invitees are required for team creation."
            );
        }

        if (inviteeIds.stream().anyMatch(inviteeId -> Objects.equals(inviteeId, creatorId))) {
            throw new IllegalArgumentException("Creator must not be included in invitees.");
        }
        ensureDistinctIds(inviteeIds, "inviteeIds");

        LocalDate today = LocalDate.now(clock);
        LocalDate expiresAt = today.plusDays(validateInvitationTtlDays());
        UUID requestId = UUID.randomUUID();

        List<TeamInviteNotification> createdInvitations = new ArrayList<>();
        for (UUID inviteeId : inviteeIds) {
            User invitee = getUserOrThrow(inviteeId);
            ensureUserHasNoTeam(invitee, "Invitee '" + inviteeId + "' already belongs to a team.");

            TeamInviteNotification invitation = new TeamInviteNotification(
                    requestId,
                    creatorId,
                    teamName,
                    invitee,
                    NOTIFICATION_PRIORITY,
                    today,
                    expiresAt,
                    minimumRequiredMembers
            );
            TeamInviteNotification saved = (TeamInviteNotification) notificationRepository.save(invitation);
            createdInvitations.add(saved);
        }

        log.info("Started team creation request requestId={} creatorId={} inviteesCount={}",
                requestId, creatorId, createdInvitations.size());

        return toRequestView(createdInvitations);
    }

    public TeamCreationRequestView getTeamCreationRequest(UUID requestId) {
        requireNonNullId(requestId, "requestId");
        List<TeamInviteNotification> requestInvitations = getRequestInvitationsOrThrow(requestId);
        requestInvitations = refreshRequestExpirationIfNeeded(requestInvitations);
        return toRequestView(requestInvitations);
    }

    public List<TeamCreationRequestView> getRequestsByCreator(UUID creatorId) {
        requireNonNullId(creatorId, "creatorId");
        if (!userRepository.existsById(creatorId)) {
            throw new UserNotFoundException(creatorId);
        }

        List<TeamInviteNotification> invitations = notificationRepository.findTeamInvitationsByCreatorId(creatorId);
        if (invitations.isEmpty()) {
            return List.of();
        }

        Map<UUID, List<TeamInviteNotification>> byRequest = groupByRequestId(invitations);
        List<TeamCreationRequestView> response = new ArrayList<>();
        for (Map.Entry<UUID, List<TeamInviteNotification>> entry : byRequest.entrySet()) {
            List<TeamInviteNotification> refreshed = refreshRequestExpirationIfNeeded(entry.getValue());
            response.add(toRequestView(refreshed));
        }

        return List.copyOf(response);
    }

    public List<TeamInvitationView> getInvitationsForInvitee(UUID inviteeId, TeamInvitationStatus status) {
        requireNonNullId(inviteeId, "inviteeId");
        if (!userRepository.existsById(inviteeId)) {
            throw new UserNotFoundException(inviteeId);
        }

        List<TeamInviteNotification> invitations = status == null
                ? notificationRepository.findTeamInvitationsByTargetId(inviteeId)
                : notificationRepository.findTeamInvitationsByTargetIdAndStatus(inviteeId, status);

        Set<UUID> processedRequests = new HashSet<>();
        List<TeamInvitationView> response = new ArrayList<>();
        for (TeamInviteNotification invitation : invitations) {
            if (processedRequests.add(invitation.getRequestId())) {
                refreshRequestExpirationIfNeeded(getRequestInvitationsOrThrow(invitation.getRequestId()));
            }

            TeamInviteNotification refreshed = notificationRepository.findTeamInvitationByIdAndTarget_Id(
                            invitation.getId(),
                            inviteeId
                    )
                    .orElse(invitation);
            response.add(toInvitationView(refreshed));
        }

        return List.copyOf(response);
    }

    @Transactional
    public TeamCreationRequestView cancelRequest(UUID requestId, UUID requesterId) {
        requireNonNullId(requestId, "requestId");
        requireNonNullId(requesterId, "requesterId");

        List<TeamInviteNotification> requestInvitations = getOpenRequestInvitationsOrThrow(requestId);
        TeamInviteNotification seed = seed(requestInvitations);
        if (!Objects.equals(seed.getCreatorId(), requesterId)) {
            throw new IllegalArgumentException("Only the creator can cancel the team creation request.");
        }

        LocalDate today = LocalDate.now(clock);
        for (TeamInviteNotification invitation : requestInvitations) {
            if (!invitation.isPending()) {
                continue;
            }
            invitation.cancel(today);
            invitation.setTitle("Invito annullato");
            invitation.setMessage("L'invito al team '" + invitation.getTeamName() + "' è stato annullato.");
            notificationRepository.save(invitation);
        }

        return toRequestView(getRequestInvitationsOrThrow(requestId));
    }

    @Transactional
    public TeamInvitationView acceptInvitation(UUID invitationId, UUID inviteeId) {
        requireNonNullId(invitationId, "invitationId");
        requireNonNullId(inviteeId, "inviteeId");

        TeamInviteNotification invitation = notificationRepository
                .findTeamInvitationByIdAndTarget_Id(invitationId, inviteeId)
                .orElseThrow(() -> new TeamInvitationNotFoundException(invitationId, inviteeId));

        List<TeamInviteNotification> requestInvitations = refreshRequestExpirationIfNeeded(
                getRequestInvitationsOrThrow(invitation.getRequestId())
        );

        invitation = requestInvitations.stream()
                .filter(item -> Objects.equals(item.getId(), invitationId))
                .findFirst()
                .orElseThrow(() -> new TeamInvitationNotFoundException(invitationId, inviteeId));

        if (!invitation.isPending()) {
            throw new TeamInvitationAlreadyProcessedException(invitationId);
        }

        User invitee = getUserOrThrow(inviteeId);
        ensureUserHasNoTeam(invitee, "Invitee already belongs to another team.");

        LocalDate today = LocalDate.now(clock);
        invitation.accept(today);
        notificationRepository.save(invitation);

        requestInvitations = replaceInvitation(requestInvitations, invitation);
        TeamInviteNotification seed = seed(requestInvitations);

        UUID currentTeamId = findTeamId(requestInvitations);
        if (currentTeamId == null
                && acceptedMembersIncludingCreator(requestInvitations) >= seed.getMinimumRequiredMembers()) {
            Team team = createTeamFromAcceptedInvitations(seed, requestInvitations);
            linkTeamToRequestInvitations(requestInvitations, team.getId());
        } else if (currentTeamId != null) {
            teamService.addMemberToTeam(currentTeamId, inviteeId);
        }

        return toInvitationView(invitation);
    }

    @Transactional
    public TeamInvitationView rejectInvitation(UUID invitationId, UUID inviteeId) {
        requireNonNullId(invitationId, "invitationId");
        requireNonNullId(inviteeId, "inviteeId");

        TeamInviteNotification invitation = notificationRepository
                .findTeamInvitationByIdAndTarget_Id(invitationId, inviteeId)
                .orElseThrow(() -> new TeamInvitationNotFoundException(invitationId, inviteeId));

        List<TeamInviteNotification> requestInvitations = refreshRequestExpirationIfNeeded(
                getRequestInvitationsOrThrow(invitation.getRequestId())
        );

        invitation = requestInvitations.stream()
                .filter(item -> Objects.equals(item.getId(), invitationId))
                .findFirst()
                .orElseThrow(() -> new TeamInvitationNotFoundException(invitationId, inviteeId));

        if (!invitation.isPending()) {
            throw new TeamInvitationAlreadyProcessedException(invitationId);
        }

        LocalDate today = LocalDate.now(clock);
        invitation.reject(today);
        invitation.setTitle("Invito rifiutato");
        notificationRepository.save(invitation);

        notifyCreatorAboutRejection(invitation);
        return toInvitationView(invitation);
    }

    @Transactional
    public int expireOpenRequests() {
        LocalDate today = LocalDate.now(clock);

        List<TeamInviteNotification> pendingInvitations =
                notificationRepository.findTeamInvitationsByStatus(TeamInvitationStatus.PENDING);

        Set<UUID> affectedRequestIds = new HashSet<>();
        for (TeamInviteNotification invitation : pendingInvitations) {
            if (!invitation.isExpiredAt(today)) {
                continue;
            }

            invitation.expire(today);
            invitation.setTitle("Invito scaduto");
            invitation.setMessage("L'invito al team '" + invitation.getTeamName() + "' è scaduto.");
            notificationRepository.save(invitation);
            affectedRequestIds.add(invitation.getRequestId());
        }

        for (UUID requestId : affectedRequestIds) {
            refreshRequestExpirationIfNeeded(getRequestInvitationsOrThrow(requestId));
        }

        return affectedRequestIds.size();
    }

    private List<TeamInviteNotification> refreshRequestExpirationIfNeeded(List<TeamInviteNotification> requestInvitations) {
        if (requestInvitations.isEmpty()) {
            return requestInvitations;
        }

        LocalDate today = LocalDate.now(clock);
        boolean changed = false;

        for (TeamInviteNotification invitation : requestInvitations) {
            if (invitation.isPending() && invitation.isExpiredAt(today)) {
                invitation.expire(today);
                invitation.setTitle("Invito scaduto");
                invitation.setMessage("L'invito al team '" + invitation.getTeamName() + "' è scaduto.");
                notificationRepository.save(invitation);
                changed = true;
            }
        }

        TeamInviteNotification seed = seed(requestInvitations);
        UUID currentTeamId = findTeamId(requestInvitations);
        if (currentTeamId == null
                && acceptedMembersIncludingCreator(requestInvitations) >= seed.getMinimumRequiredMembers()) {
            Team team = createTeamFromAcceptedInvitations(seed, requestInvitations);
            linkTeamToRequestInvitations(requestInvitations, team.getId());
            changed = true;
        }

        if (!changed) {
            return requestInvitations;
        }

        return getRequestInvitationsOrThrow(seed.getRequestId());
    }

    private Team createTeamFromAcceptedInvitations(
            TeamInviteNotification seed,
            List<TeamInviteNotification> requestInvitations
    ) {
        User creator = getUserOrThrow(seed.getCreatorId());
        ensureUserHasNoTeam(creator, "Creator already belongs to another team.");

        List<User> members = new ArrayList<>();
        members.add(creator);

        for (TeamInviteNotification invitation : requestInvitations) {
            if (invitation.getInvitationStatus() != TeamInvitationStatus.ACCEPTED) {
                continue;
            }

            User member = getUserOrThrow(invitation.getTarget().getId());
            ensureUserHasNoTeam(member, "Invitee already belongs to another team.");
            members.add(member);
        }

        Team team = teamService.createTeam(seed.getTeamName(), members);
        log.info("Created team teamId={} from requestId={}", team.getId(), seed.getRequestId());
        return team;
    }

    private void linkTeamToRequestInvitations(List<TeamInviteNotification> requestInvitations, UUID teamId) {
        for (TeamInviteNotification invitation : requestInvitations) {
            invitation.setTeamId(teamId);
            notificationRepository.save(invitation);
        }
    }

    private void notifyCreatorAboutRejection(TeamInviteNotification invitation) {
        User creator = getUserOrThrow(invitation.getCreatorId());
        User invitee = getUserOrThrow(invitation.getTarget().getId());
        domainEventPublisher.publish(new TeamInvitationRejectedEvent(creator, invitee, invitation.getTeamName()));
    }

    private List<TeamInviteNotification> getOpenRequestInvitationsOrThrow(UUID requestId) {
        List<TeamInviteNotification> invitations = refreshRequestExpirationIfNeeded(getRequestInvitationsOrThrow(requestId));
        TeamCreationRequestStatus status = evaluateRequestStatus(invitations);
        if (status != TeamCreationRequestStatus.OPEN) {
            throw new TeamCreationRequestClosedException(requestId);
        }
        return invitations;
    }

    private List<TeamInviteNotification> getRequestInvitationsOrThrow(UUID requestId) {
        List<TeamInviteNotification> invitations = notificationRepository.findTeamInvitationsByRequestId(requestId);
        if (invitations.isEmpty()) {
            throw new TeamCreationRequestNotFoundException(requestId);
        }
        return List.copyOf(invitations);
    }

    private TeamCreationRequestView toRequestView(List<TeamInviteNotification> invitations) {
        TeamInviteNotification seed = seed(invitations);
        TeamCreationRequestStatus status = evaluateRequestStatus(invitations);

        List<TeamInvitationView> invitationViews = invitations.stream()
                .map(this::toInvitationView)
                .toList();

        return new TeamCreationRequestView(
                seed.getRequestId(),
                seed.getCreatorId(),
                seed.getTeamName(),
                status,
                seed.getSentAt(),
                seed.getExpiresAt(),
                seed.getMinimumRequiredMembers(),
                findTeamId(invitations),
                invitationViews
        );
    }

    private TeamInvitationView toInvitationView(TeamInviteNotification invitation) {
        return new TeamInvitationView(
                invitation.getId(),
                invitation.getRequestId(),
                invitation.getTarget().getId(),
                invitation.getInvitationStatus(),
                invitation.getSentAt(),
                invitation.getRespondedAt()
        );
    }

    private TeamCreationRequestStatus evaluateRequestStatus(List<TeamInviteNotification> invitations) {
        if (findTeamId(invitations) != null) {
            return TeamCreationRequestStatus.COMPLETED;
        }

        boolean hasPending = invitations.stream().anyMatch(TeamInviteNotification::isPending);
        if (hasPending) {
            return TeamCreationRequestStatus.OPEN;
        }

        boolean allCancelled = invitations.stream()
                .allMatch(invitation -> invitation.getInvitationStatus() == TeamInvitationStatus.CANCELLED);
        if (allCancelled) {
            return TeamCreationRequestStatus.CANCELLED;
        }

        return TeamCreationRequestStatus.FAILED;
    }

    private static Map<UUID, List<TeamInviteNotification>> groupByRequestId(List<TeamInviteNotification> invitations) {
        Map<UUID, List<TeamInviteNotification>> grouped = new LinkedHashMap<>();
        for (TeamInviteNotification invitation : invitations) {
            grouped.computeIfAbsent(invitation.getRequestId(), ignored -> new ArrayList<>()).add(invitation);
        }
        return grouped;
    }

    private static TeamInviteNotification seed(List<TeamInviteNotification> invitations) {
        if (invitations == null || invitations.isEmpty()) {
            throw new IllegalArgumentException("invitations must not be null or empty.");
        }
        return invitations.getFirst();
    }

    private static UUID findTeamId(List<TeamInviteNotification> invitations) {
        return invitations.stream()
                .map(TeamInviteNotification::getTeamId)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private static int acceptedMembersIncludingCreator(List<TeamInviteNotification> invitations) {
        int acceptedInvitations = (int) invitations.stream()
                .filter(invitation -> invitation.getInvitationStatus() == TeamInvitationStatus.ACCEPTED)
                .count();
        return acceptedInvitations + 1;
    }

    private static List<TeamInviteNotification> replaceInvitation(
            List<TeamInviteNotification> invitations,
            TeamInviteNotification updatedInvitation
    ) {
        List<TeamInviteNotification> updated = new ArrayList<>(invitations.size());
        for (TeamInviteNotification invitation : invitations) {
            if (Objects.equals(invitation.getId(), updatedInvitation.getId())) {
                updated.add(updatedInvitation);
            } else {
                updated.add(invitation);
            }
        }
        return List.copyOf(updated);
    }

    private int validateMinimumMembers() {
        int minMembers = teamCreationProperties.getMinMembers();
        if (minMembers < 2) {
            throw new IllegalStateException("app.team.min-members must be >= 2.");
        }
        return minMembers;
    }

    private int validateInvitationTtlDays() {
        int ttlDays = teamCreationProperties.getInvitationTtlDays();
        if (ttlDays < 1) {
            throw new IllegalStateException("app.team.invitation-ttl-days must be >= 1.");
        }
        return ttlDays;
    }

    private static void validateInvitees(List<UUID> inviteeIds) {
        if (inviteeIds == null || inviteeIds.isEmpty()) {
            throw new IllegalArgumentException("inviteeIds must not be null or empty.");
        }
        if (inviteeIds.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("inviteeIds must not contain null values.");
        }
    }

    private User getUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private static void ensureDistinctIds(List<UUID> ids, String fieldName) {
        Set<UUID> seen = new HashSet<>();
        for (UUID id : ids) {
            if (!seen.add(id)) {
                throw new IllegalArgumentException(fieldName + " must not contain duplicates.");
            }
        }
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }

    private static void requireNonNullId(UUID value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null.");
        }
    }

    private static void ensureUserHasNoTeam(User user, String message) {
        if (user.getTeam() != null) {
            throw new IllegalArgumentException(message);
        }
    }
}

package org.da_scegliere.progetto_ids_hackathon.core.entities.notification;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.da_scegliere.progetto_ids_hackathon.core.entities.user.User;
import org.da_scegliere.progetto_ids_hackathon.core.enums.state.team.TeamInvitationStatus;
import org.da_scegliere.progetto_ids_hackathon.core.events.team.TeamInvitationRejectedEvent;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Notification specialization used to handle team-creation invitations.
 */
@Getter
@Setter
@Entity
public class TeamInviteNotification extends BaseNotification {

    @NotNull
    private UUID requestId;

    @NotNull
    private UUID creatorId;

    @NotBlank
    private String teamName;

    @NotNull
    private LocalDate sentAt;

    @NotNull
    private LocalDate expiresAt;

    @NotNull
    private Integer minimumRequiredMembers;

    private UUID teamId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TeamInvitationStatus invitationStatus;

    private LocalDate respondedAt;

    public TeamInviteNotification(
            UUID requestId,
            UUID creatorId,
            String teamName,
            User invitee,
            int priority,
            LocalDate sentAt,
            LocalDate expiresAt,
            int minimumRequiredMembers
    ) {
        super(
                "Invito team",
                "Hai ricevuto un invito al team '" + teamName + "'.",
                invitee,
                priority
        );
        this.requestId = requestId;
        this.creatorId = creatorId;
        this.teamName = teamName;
        this.sentAt = sentAt;
        this.expiresAt = expiresAt;
        this.minimumRequiredMembers = minimumRequiredMembers;
        this.invitationStatus = TeamInvitationStatus.PENDING;
    }

    public TeamInviteNotification() {
        super();
    }

    public boolean isPending() {
        return invitationStatus == TeamInvitationStatus.PENDING;
    }

    public boolean isExpiredAt(LocalDate date) {
        return date != null && date.isAfter(expiresAt);
    }

    public void accept(LocalDate date) {
        ensurePending();
        this.invitationStatus = TeamInvitationStatus.ACCEPTED;
        this.respondedAt = date;
    }

    public void reject(LocalDate date) {
        ensurePending();
        this.invitationStatus = TeamInvitationStatus.REJECTED;
        this.respondedAt = date;
    }

    public void cancel(LocalDate date) {
        ensurePending();
        this.invitationStatus = TeamInvitationStatus.CANCELLED;
        this.respondedAt = date;
    }

    public void expire(LocalDate date) {
        ensurePending();
        this.invitationStatus = TeamInvitationStatus.EXPIRED;
        this.respondedAt = date;
    }

    public TeamInvitationRejectedEvent toRejectedEvent(User creator, User invitee) {
        return new TeamInvitationRejectedEvent(creator, invitee, teamName);
    }

    private void ensurePending() {
        if (!isPending()) {
            throw new IllegalStateException("Team invitation is already processed.");
        }
    }
}

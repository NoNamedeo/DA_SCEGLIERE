package org.da_scegliere.progetto_ids_hackathon.presentation.mapper;

import org.da_scegliere.progetto_ids_hackathon.application.services.views.team.TeamCreationRequestView;
import org.da_scegliere.progetto_ids_hackathon.application.services.views.team.TeamInvitationView;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.team.TeamCreationRequestResponse;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.team.TeamInvitationResponse;

import java.util.List;

public final class TeamInvitationMapper {

    private TeamInvitationMapper() {
    }

    public static TeamCreationRequestResponse toTeamCreationRequestResponse(TeamCreationRequestView request) {
        return new TeamCreationRequestResponse(
                request.id(),
                request.creatorId(),
                request.teamName(),
                request.status(),
                request.createdAt(),
                request.expiresAt(),
                request.minimumRequiredMembers(),
                request.teamId(),
                request.invitations().stream()
                        .map(TeamInvitationMapper::toTeamInvitationResponse)
                        .toList()
        );
    }

    public static TeamInvitationResponse toTeamInvitationResponse(TeamInvitationView invitation) {
        return new TeamInvitationResponse(
                invitation.id(),
                invitation.requestId(),
                invitation.inviteeId(),
                invitation.status(),
                invitation.sentAt(),
                invitation.respondedAt()
        );
    }

    public static List<TeamInvitationResponse> toTeamInvitationResponseList(List<TeamInvitationView> invitations) {
        return invitations.stream().map(TeamInvitationMapper::toTeamInvitationResponse).toList();
    }
}

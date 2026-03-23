package org.da_scegliere.progetto_ids_hackathon.application.services.views.team;

import org.da_scegliere.progetto_ids_hackathon.core.enums.state.team.TeamCreationRequestStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TeamCreationRequestView(
        UUID id,
        UUID creatorId,
        String teamName,
        TeamCreationRequestStatus status,
        LocalDate createdAt,
        LocalDate expiresAt,
        int minimumRequiredMembers,
        UUID teamId,
        List<TeamInvitationView> invitations
) {
}

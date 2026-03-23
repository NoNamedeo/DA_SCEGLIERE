package org.da_scegliere.progetto_ids_hackathon.application.services.views.team;

import org.da_scegliere.progetto_ids_hackathon.core.enums.state.team.TeamInvitationStatus;

import java.time.LocalDate;
import java.util.UUID;

public record TeamInvitationView(
        UUID id,
        UUID requestId,
        UUID inviteeId,
        TeamInvitationStatus status,
        LocalDate sentAt,
        LocalDate respondedAt
) {
}

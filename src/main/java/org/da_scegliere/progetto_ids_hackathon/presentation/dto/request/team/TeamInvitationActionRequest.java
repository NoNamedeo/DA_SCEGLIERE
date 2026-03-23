package org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.team;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TeamInvitationActionRequest(
        @NotNull UUID inviteeId
) {
}


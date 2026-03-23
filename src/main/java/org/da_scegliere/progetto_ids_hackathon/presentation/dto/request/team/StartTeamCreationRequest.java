package org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.team;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record StartTeamCreationRequest(
        @NotNull UUID creatorId,
        @NotBlank String teamName,
        @NotEmpty List<@NotNull UUID> inviteeIds
) {
}


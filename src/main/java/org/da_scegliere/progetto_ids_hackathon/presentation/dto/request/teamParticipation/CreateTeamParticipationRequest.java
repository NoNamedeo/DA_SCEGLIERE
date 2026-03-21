package org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.teamParticipation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateTeamParticipationRequest(
        @NotNull UUID hackathonId,
        @NotNull UUID teamId,
        @NotBlank @Size(max = 100) String nickname
) {
}

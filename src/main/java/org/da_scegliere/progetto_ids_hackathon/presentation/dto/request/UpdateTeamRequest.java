package org.da_scegliere.progetto_ids_hackathon.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateTeamRequest(
        @NotBlank String name
) {
}

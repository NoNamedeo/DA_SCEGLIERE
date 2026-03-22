package org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.manager;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateManagerRequest(
        @NotBlank @Size(min = 2, max = 50) String name
) {
}

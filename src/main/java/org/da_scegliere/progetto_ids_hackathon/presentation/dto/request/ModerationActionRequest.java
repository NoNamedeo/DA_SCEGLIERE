package org.da_scegliere.progetto_ids_hackathon.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ModerationActionRequest(
        @NotBlank @Size(max = 500) String reason
) {
}

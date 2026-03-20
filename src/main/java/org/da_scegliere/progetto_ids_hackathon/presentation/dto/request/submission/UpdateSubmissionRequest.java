package org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.submission;

import jakarta.validation.constraints.NotBlank;

public record UpdateSubmissionRequest(
        @NotBlank String title,
        @NotBlank String description
) {
}

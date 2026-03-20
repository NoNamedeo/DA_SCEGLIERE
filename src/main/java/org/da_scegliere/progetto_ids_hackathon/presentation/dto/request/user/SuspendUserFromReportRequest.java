package org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SuspendUserFromReportRequest(
        @NotBlank @Size(max = 500) String suspensionReason,
        @NotBlank @Size(max = 500) String reportResolutionNotes
) {
}

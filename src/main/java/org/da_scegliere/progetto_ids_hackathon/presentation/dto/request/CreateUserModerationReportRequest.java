package org.da_scegliere.progetto_ids_hackathon.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.da_scegliere.progetto_ids_hackathon.core.enums.report.ReporterType;

import java.util.UUID;

public record CreateUserModerationReportRequest(
        @NotNull UUID reporterId,
        @NotNull ReporterType reporterType,
        @NotNull UUID reportedUserId,
        @NotBlank String title,
        @NotBlank String description
) {
}

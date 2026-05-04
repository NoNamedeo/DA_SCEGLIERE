package org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.moderation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.da_scegliere.progetto_ids_hackathon.core.enums.report.ReporterType;

import java.util.UUID;

public record CreateTeamParticipationModerationReportRequest(
        @NotNull UUID reporterId,
        @NotNull ReporterType reporterType,
        @NotNull UUID reportedTeamParticipationId,
        @NotBlank String title,
        @NotBlank String description
) {
}

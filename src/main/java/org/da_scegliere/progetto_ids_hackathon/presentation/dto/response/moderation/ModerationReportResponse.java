package org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.moderation;

import org.da_scegliere.progetto_ids_hackathon.core.enums.report.ReporterType;
import org.da_scegliere.progetto_ids_hackathon.core.enums.state.report.UserReportState;

import java.time.LocalDateTime;
import java.util.UUID;

public record ModerationReportResponse(
        UUID id,
        String targetType,
        UUID targetId,
        UUID reporterId,
        ReporterType reporterType,
        UserReportState state,
        String title,
        String description,
        LocalDateTime createdAt,
        LocalDateTime processedAt,
        UUID processedByManagerId,
        String processingNotes
) {
}

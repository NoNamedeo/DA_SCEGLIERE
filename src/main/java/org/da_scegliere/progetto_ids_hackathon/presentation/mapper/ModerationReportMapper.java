package org.da_scegliere.progetto_ids_hackathon.presentation.mapper;

import org.da_scegliere.progetto_ids_hackathon.core.entities.moderation.ModerationReport;
import org.da_scegliere.progetto_ids_hackathon.core.entities.moderation.StaffReport;
import org.da_scegliere.progetto_ids_hackathon.core.entities.moderation.UserReport;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.moderation.ModerationReportResponse;

import java.util.UUID;

public final class ModerationReportMapper {

    private ModerationReportMapper() {
    }

    public static ModerationReportResponse toResponse(ModerationReport report) {
        String targetType;
        UUID targetId;

        if (report instanceof UserReport userReport) {
            targetType = "USER";
            targetId = userReport.getReportedUserId();
        } else if (report instanceof StaffReport staffReport) {
            targetType = "STAFF";
            targetId = staffReport.getReportedStaffMemberId();
        } else {
            targetType = "UNKNOWN";
            targetId = null;
        }

        UUID processedByManagerId = report.getProcessedBy() != null ? report.getProcessedBy().getId() : null;
        return new ModerationReportResponse(
                report.getId(),
                targetType,
                targetId,
                report.getReporterId(),
                report.getReporterType(),
                report.getState(),
                report.getTitle(),
                report.getDescription(),
                report.getCreatedAt(),
                report.getProcessedAt(),
                processedByManagerId,
                report.getProcessingNotes()
        );
    }
}

package org.da_scegliere.progetto_ids_hackathon.infrastructure.strategies.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ExternalCalendarBookedSlotPayload(
        UUID supportRequestId,
        UUID teamId,
        UUID mentorAssignmentId,
        String mentorName,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String title
) {
}

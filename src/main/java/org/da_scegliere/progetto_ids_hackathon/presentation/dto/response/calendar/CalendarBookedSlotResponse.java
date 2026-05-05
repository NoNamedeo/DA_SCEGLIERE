package org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.calendar;

import java.time.LocalDateTime;
import java.util.UUID;

public record CalendarBookedSlotResponse(
        UUID supportRequestId,
        UUID teamId,
        UUID mentorAssignmentId,
        String mentorName,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String title
) {
}

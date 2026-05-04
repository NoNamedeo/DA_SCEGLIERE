package org.da_scegliere.progetto_ids_hackathon.application.services.views.calendar;

import java.time.LocalDateTime;
import java.util.UUID;

public record CalendarBookedSlotView(
        UUID supportRequestId,
        UUID teamId,
        UUID mentorAssignmentId,
        String mentorName,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String title
) {
}

package org.da_scegliere.progetto_ids_hackathon.presentation.mapper;

import org.da_scegliere.progetto_ids_hackathon.application.services.views.calendar.CalendarBookedSlotView;
import org.da_scegliere.progetto_ids_hackathon.application.services.views.calendar.CalendarView;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.calendar.CalendarBookedSlotResponse;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.calendar.CalendarResponse;

public final class CalendarMapper {

    private CalendarMapper() {
    }

    public static CalendarResponse toResponse(CalendarView view) {
        return new CalendarResponse(
                view.ownerType(),
                view.ownerId(),
                view.ownerName(),
                view.bookedSlots().stream().map(CalendarMapper::toResponse).toList()
        );
    }

    private static CalendarBookedSlotResponse toResponse(CalendarBookedSlotView view) {
        return new CalendarBookedSlotResponse(
                view.supportRequestId(),
                view.teamId(),
                view.mentorAssignmentId(),
                view.mentorName(),
                view.startAt(),
                view.endAt(),
                view.title()
        );
    }
}

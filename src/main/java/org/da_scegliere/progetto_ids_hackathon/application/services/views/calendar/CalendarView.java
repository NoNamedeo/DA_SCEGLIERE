package org.da_scegliere.progetto_ids_hackathon.application.services.views.calendar;

import java.util.List;
import java.util.UUID;

public record CalendarView(
        String ownerType,
        UUID ownerId,
        String ownerName,
        List<CalendarBookedSlotView> bookedSlots
) {
}

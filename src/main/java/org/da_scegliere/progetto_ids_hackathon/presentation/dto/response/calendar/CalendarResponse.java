package org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.calendar;

import java.util.List;
import java.util.UUID;

public record CalendarResponse(
        String ownerType,
        UUID ownerId,
        String ownerName,
        List<CalendarBookedSlotResponse> bookedSlots
) {
}

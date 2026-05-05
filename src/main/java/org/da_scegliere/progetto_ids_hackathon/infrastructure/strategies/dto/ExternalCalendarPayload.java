package org.da_scegliere.progetto_ids_hackathon.infrastructure.strategies.dto;

import java.util.List;

public record ExternalCalendarPayload(
        String ownerName,
        List<ExternalCalendarBookedSlotPayload> bookedSlots
) {
}

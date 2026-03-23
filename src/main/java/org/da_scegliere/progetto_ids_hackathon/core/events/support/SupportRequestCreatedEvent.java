package org.da_scegliere.progetto_ids_hackathon.core.events.support;

import org.da_scegliere.progetto_ids_hackathon.core.entities.staff.StaffMember;

import java.time.LocalDate;
import java.util.List;

public record SupportRequestCreatedEvent(
        String sendingTeamName,
        LocalDate dateSlot,
        List<StaffMember> recipients
) {
    public SupportRequestCreatedEvent {
        recipients = List.copyOf(recipients);
    }
}

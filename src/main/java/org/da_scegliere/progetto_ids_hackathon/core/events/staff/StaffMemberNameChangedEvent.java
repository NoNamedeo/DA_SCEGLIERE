package org.da_scegliere.progetto_ids_hackathon.core.events.staff;

import org.da_scegliere.progetto_ids_hackathon.core.entities.staff.StaffMember;

public record StaffMemberNameChangedEvent(StaffMember staffMember, String newName) {
}

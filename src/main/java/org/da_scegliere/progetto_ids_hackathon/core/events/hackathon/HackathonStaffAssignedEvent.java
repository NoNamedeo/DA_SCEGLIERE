package org.da_scegliere.progetto_ids_hackathon.core.events.hackathon;

import org.da_scegliere.progetto_ids_hackathon.core.entities.staff.StaffMember;
import org.da_scegliere.progetto_ids_hackathon.core.enums.StaffRole;

public record HackathonStaffAssignedEvent(StaffMember staffMember, StaffRole role) {
}

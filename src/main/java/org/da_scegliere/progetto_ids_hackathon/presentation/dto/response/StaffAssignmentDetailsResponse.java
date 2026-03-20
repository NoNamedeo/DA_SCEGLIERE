package org.da_scegliere.progetto_ids_hackathon.presentation.dto.response;

import org.da_scegliere.progetto_ids_hackathon.core.enums.StaffRole;

import java.time.LocalDate;
import java.util.UUID;

public record StaffAssignmentDetailsResponse(
        UUID id,
        UUID staffMemberId,
        UUID hackathonId,
        StaffRole role,
        LocalDate assignmentDate
) {
}

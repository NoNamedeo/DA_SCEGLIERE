package org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.staff;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateStaffMemberRequest(
        @NotBlank @Size(min = 2, max = 50) String name
) {
}

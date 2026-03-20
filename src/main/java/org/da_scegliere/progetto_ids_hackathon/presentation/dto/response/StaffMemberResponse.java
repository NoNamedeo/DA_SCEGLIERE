package org.da_scegliere.progetto_ids_hackathon.presentation.dto.response;

import org.da_scegliere.progetto_ids_hackathon.core.enums.state.user.AccountState;

import java.util.UUID;

public record StaffMemberResponse(
        UUID id,
        String name,
        int age,
        String email,
        AccountState accountStatus
) {
}

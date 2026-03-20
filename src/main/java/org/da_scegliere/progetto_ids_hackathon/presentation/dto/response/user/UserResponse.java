package org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.user;

import org.da_scegliere.progetto_ids_hackathon.core.enums.state.user.AccountState;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        int age,
        String email,
        AccountState accountStatus,
        UUID teamId
) {
}

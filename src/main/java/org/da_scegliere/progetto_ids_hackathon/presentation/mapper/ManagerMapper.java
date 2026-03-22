package org.da_scegliere.progetto_ids_hackathon.presentation.mapper;

import org.da_scegliere.progetto_ids_hackathon.core.entities.user.Manager;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.manager.ManagerResponse;

public class ManagerMapper {

    public static ManagerResponse toResponse(Manager manager) {
        return new ManagerResponse(
                manager.getId(),
                manager.getName(),
                manager.getAge(),
                manager.getEmail(),
                manager.getAccountStatus()
        );
    }
}

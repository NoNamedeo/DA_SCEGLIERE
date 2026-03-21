package org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.team;

import java.util.List;
import java.util.UUID;

public record TeamResponse(
        UUID id,
        String name,
        List<UUID> memberIds
) {
}

package org.da_scegliere.progetto_ids_hackathon.presentation.dto.response;

import java.time.LocalDate;
import java.util.UUID;

public record TeamParticipationResponse(
        UUID id,
        String nickname,
        LocalDate entryDate,
        UUID teamId,
        UUID hackathonId
) {
}

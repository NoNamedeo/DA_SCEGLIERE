package org.da_scegliere.progetto_ids_hackathon.presentation.dto.response;

import org.da_scegliere.progetto_ids_hackathon.core.enums.state.hackathon.HackathonState;

import java.util.UUID;

public record HackathonStateResponse(
        UUID hackathonId,
        HackathonState state
) {
}

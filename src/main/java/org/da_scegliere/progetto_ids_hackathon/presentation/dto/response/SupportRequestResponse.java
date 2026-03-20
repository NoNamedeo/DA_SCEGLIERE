package org.da_scegliere.progetto_ids_hackathon.presentation.dto.response;

import org.da_scegliere.progetto_ids_hackathon.core.enums.state.support.SupportRequestState;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record SupportRequestResponse(
        UUID id,
        LocalDate dateSlot,
        SupportRequestState state,
        UUID sendingTeamId,
        UUID acceptingMentorAssignmentId,
        List<UUID> selectedMentorAssignmentIds
) {
}

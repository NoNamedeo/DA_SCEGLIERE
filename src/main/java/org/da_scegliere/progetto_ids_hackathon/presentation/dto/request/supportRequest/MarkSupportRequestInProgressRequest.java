package org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.supportRequest;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MarkSupportRequestInProgressRequest(
        @NotNull UUID acceptingMentorAssignmentId
) {
}

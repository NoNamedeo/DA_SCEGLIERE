package org.da_scegliere.progetto_ids_hackathon.presentation.dto.response;

import java.time.LocalDate;
import java.util.UUID;

public record HackathonSummaryResponse(
        UUID id,
        String name,
        String description,
        LocalDate registrationDeadline,
        LocalDate submissionDeadline,
        LocalDate evaluationDeadline
) {
}

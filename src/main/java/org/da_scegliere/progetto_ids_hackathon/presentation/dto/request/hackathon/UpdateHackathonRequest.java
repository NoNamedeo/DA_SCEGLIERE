package org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.hackathon;

import java.time.LocalDate;

public record UpdateHackathonRequest(
        String description,
        LocalDate registrationDeadline,
        LocalDate submissionDeadline,
        LocalDate evaluationDeadline
) {
}

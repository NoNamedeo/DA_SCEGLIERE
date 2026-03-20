package org.da_scegliere.progetto_ids_hackathon.presentation.dto.response;

import java.time.LocalDate;
import java.util.UUID;

public record SubmissionResponse(
        UUID id,
        String title,
        String description,
        Integer judgeScore,
        String judgeJudgement,
        LocalDate submittedAt,
        LocalDate evaluatedAt,
        UUID teamParticipationId
) {
}

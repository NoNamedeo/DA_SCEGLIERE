package org.da_scegliere.progetto_ids_hackathon.presentation.mapper;

import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Submission;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.submission.SubmissionResponse;

import java.util.UUID;

public final class SubmissionMapper {

    private SubmissionMapper() {
    }

    public static SubmissionResponse toResponse(Submission submission) {
        UUID teamParticipationId = submission.getTeamParticipation() != null
                ? submission.getTeamParticipation().getId()
                : null;
        return new SubmissionResponse(
                submission.getId(),
                submission.getTitle(),
                submission.getDescription(),
                submission.getJudgeScore(),
                submission.getJudgeJudgement(),
                submission.getSubmittedAt(),
                submission.getEvaluatedAt(),
                teamParticipationId
        );
    }
}


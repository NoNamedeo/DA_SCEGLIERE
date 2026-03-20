package org.da_scegliere.progetto_ids_hackathon.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.da_scegliere.progetto_ids_hackathon.application.services.TeamParticipationService;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Submission;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.EvaluateSubmissionRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.UpdateSubmissionRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.SubmissionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/submissions")
public class SubmissionController {

    private final TeamParticipationService teamParticipationService;

    @GetMapping("/{submissionId}")
    public ResponseEntity<SubmissionResponse> getSubmission(@PathVariable UUID submissionId) {
        return ResponseEntity.ok(toResponse(teamParticipationService.getSubmissionById(submissionId)));
    }

    @PatchMapping("/{submissionId}")
    public ResponseEntity<SubmissionResponse> updateSubmission(
            @PathVariable UUID submissionId,
            @Valid @RequestBody UpdateSubmissionRequest request
    ) {
        Submission updated = teamParticipationService.updateSubmission(
                submissionId,
                request.title(),
                request.description()
        );
        return ResponseEntity.ok(toResponse(updated));
    }

    @PostMapping("/{submissionId}/evaluations")
    public ResponseEntity<SubmissionResponse> evaluateSubmission(
            @PathVariable UUID submissionId,
            @Valid @RequestBody EvaluateSubmissionRequest request
    ) {
        Submission evaluated = teamParticipationService.evaluateSubmission(submissionId, request.score(), request.judgement());
        return ResponseEntity.ok(toResponse(evaluated));
    }

    @PatchMapping("/{submissionId}/evaluations")
    public ResponseEntity<SubmissionResponse> updateSubmissionEvaluation(
            @PathVariable UUID submissionId,
            @Valid @RequestBody EvaluateSubmissionRequest request
    ) {
        Submission updated = teamParticipationService.updateSubmissionEvaluation(
                submissionId,
                request.score(),
                request.judgement()
        );
        return ResponseEntity.ok(toResponse(updated));
    }

    @DeleteMapping("/{submissionId}")
    public ResponseEntity<Void> deleteSubmission(@PathVariable UUID submissionId) {
        teamParticipationService.deleteSubmission(submissionId);
        return ResponseEntity.noContent().build();
    }

    static SubmissionResponse toResponse(Submission submission) {
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

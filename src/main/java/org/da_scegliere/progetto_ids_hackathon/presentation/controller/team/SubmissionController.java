/*
 * Authors:  Alejandro Innocenzi, Matteo Vittori, Vladislav Gaspari
 * Copyright (c) 2026 Alejandro Innocenzi, Matteo Vittori, Vladislav Gaspari. All rights reserved.
 *
 * This file is part of the DA_SCEGLIERE project. Unauthorized copying,
 * distribution, modification, or use of this file, via any medium,
 * is strictly prohibited unless in compliance with the license.
 *
 * Licensed under the MIT License:
 *     - Permission is hereby granted, free of charge, to any person obtaining
 *       a copy of this software and associated documentation files (the "Software"),
 *       to deal in the Software without restriction, including without limitation
 *       the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *       and/or sell copies of the Software, and to permit persons to whom the
 *       Software is furnished to do so, subject to the following conditions:
 *
 *     - The above copyright notice and this permission notice shall be included
 *       in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package org.da_scegliere.progetto_ids_hackathon.presentation.controller.team;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.da_scegliere.progetto_ids_hackathon.application.services.TeamParticipationService;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Submission;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.submission.EvaluateSubmissionRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.submission.UpdateSubmissionRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.submission.SubmissionResponse;
import org.da_scegliere.progetto_ids_hackathon.presentation.mapper.SubmissionMapper;
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
        return ResponseEntity.ok(SubmissionMapper.toResponse(teamParticipationService.getSubmissionById(submissionId)));
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
        return ResponseEntity.ok(SubmissionMapper.toResponse(updated));
    }

    @PostMapping("/{submissionId}/evaluations")
    public ResponseEntity<SubmissionResponse> evaluateSubmission(
            @PathVariable UUID submissionId,
            @Valid @RequestBody EvaluateSubmissionRequest request
    ) {
        Submission evaluated = teamParticipationService.evaluateSubmission(submissionId, request.score(), request.judgement());
        return ResponseEntity.ok(SubmissionMapper.toResponse(evaluated));
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
        return ResponseEntity.ok(SubmissionMapper.toResponse(updated));
    }

    @DeleteMapping("/{submissionId}")
    public ResponseEntity<Void> deleteSubmission(@PathVariable UUID submissionId) {
        teamParticipationService.deleteSubmission(submissionId);
        return ResponseEntity.noContent().build();
    }
}

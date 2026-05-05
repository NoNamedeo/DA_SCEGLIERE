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
import org.da_scegliere.progetto_ids_hackathon.application.services.moderation.ModerationReportService;
import org.da_scegliere.progetto_ids_hackathon.core.entities.moderation.TeamParticipationReport;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Submission;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.TeamParticipation;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.submission.CreateSubmissionRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.teamParticipation.CreateTeamParticipationRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.moderation.ModerationReportResponse;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.submission.SubmissionResponse;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.team.TeamParticipationResponse;
import org.da_scegliere.progetto_ids_hackathon.presentation.mapper.ModerationReportMapper;
import org.da_scegliere.progetto_ids_hackathon.presentation.mapper.SubmissionMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/team-participations")
public class TeamParticipationController {

    private final TeamParticipationService teamParticipationService;
    private final ModerationReportService moderationReportService;

    @PostMapping
    public ResponseEntity<Void> createTeamParticipation(
            @Valid @RequestBody CreateTeamParticipationRequest request
    ) {
        TeamParticipation createdParticipation = teamParticipationService.createTeamParticipation(
                request.hackathonId(),
                request.teamId(),
                request.nickname()
        );

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{participationId}")
                .buildAndExpand(createdParticipation.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{participationId}")
    public ResponseEntity<TeamParticipationResponse> getTeamParticipation(@PathVariable UUID participationId) {
        TeamParticipation participation = teamParticipationService.getTeamParticipationById(participationId);
        return ResponseEntity.ok(toResponse(participation));
    }

    @GetMapping("/{participationId}/submissions")
    public ResponseEntity<List<SubmissionResponse>> getSubmissions(@PathVariable UUID participationId) {
        List<SubmissionResponse> responses = teamParticipationService.getSubmissionsByTeamParticipation(participationId)
                .stream()
                .map(SubmissionMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{participationId}/moderation-reports")
    public ResponseEntity<List<ModerationReportResponse>> getModerationReports(@PathVariable UUID participationId) {
        List<TeamParticipationReport> reports = moderationReportService.getReportsByTeamParticipationId(participationId);
        return ResponseEntity.ok(reports.stream().map(ModerationReportMapper::toResponse).toList());
    }

    @PostMapping("/{participationId}/submissions")
    public ResponseEntity<Void> createSubmission(
            @PathVariable UUID participationId,
            @Valid @RequestBody CreateSubmissionRequest request
    ) {
        Submission createdSubmission = teamParticipationService.createSubmission(
                participationId,
                request.title(),
                request.description()
        );

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/submissions/{submissionId}")
                .buildAndExpand(createdSubmission.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    public static TeamParticipationResponse toResponse(TeamParticipation participation) {
        UUID teamId = participation.getTeam() != null ? participation.getTeam().getId() : null;
        UUID hackathonId = participation.getHackathon() != null ? participation.getHackathon().getId() : null;

        return new TeamParticipationResponse(
                participation.getId(),
                participation.getNickname(),
                participation.getEntryDate(),
                teamId,
                hackathonId,
                participation.isDisqualified(),
                participation.getDisqualifiedAt(),
                participation.getDisqualificationReason()
        );
    }
}

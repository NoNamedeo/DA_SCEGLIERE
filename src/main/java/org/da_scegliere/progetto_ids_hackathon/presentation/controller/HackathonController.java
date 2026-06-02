/*
 * Authors:  Alejandro Innocenzi, Matteo Vittori
 * Copyright (c) 2026 Alejandro Innocenzi, Matteo Vittori. All rights reserved.
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

package org.da_scegliere.progetto_ids_hackathon.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.ITeamParticipationRepository;
import org.da_scegliere.progetto_ids_hackathon.application.services.TeamParticipationService;
import org.da_scegliere.progetto_ids_hackathon.application.services.hackathon.HackathonCrudService;
import org.da_scegliere.progetto_ids_hackathon.application.services.hackathon.HackathonLifecycleService;
import org.da_scegliere.progetto_ids_hackathon.application.services.hackathon.HackathonStaffService;
import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.Hackathon;
import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.HackathonTimeline;
import org.da_scegliere.progetto_ids_hackathon.core.enums.state.hackathon.HackathonState;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.hackathon.AssignStaffRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.staff.AddStaffAssignmentsRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.hackathon.AssignWinnerRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.hackathon.CreateHackathonRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.hackathon.UpdateHackathonRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.hackathon.FullHackathonResponse;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.hackathon.HackathonStateResponse;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.hackathon.PublicHackathonResponse;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.submission.SubmissionResponse;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.team.TeamResponse;
import org.da_scegliere.progetto_ids_hackathon.presentation.mapper.HackathonMapper;
import org.da_scegliere.progetto_ids_hackathon.presentation.mapper.SubmissionMapper;
import org.da_scegliere.progetto_ids_hackathon.presentation.mapper.TeamMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.management.relation.InvalidRoleValueException;
import java.net.URI;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/hackathons")
public class HackathonController {

    private final HackathonCrudService hackathonCrudService;
    private final HackathonStaffService hackathonStaffService;
    private final HackathonLifecycleService hackathonLifecycleService;
    private final TeamParticipationService teamParticipationService;
    private final Clock clock;

    @GetMapping
    public ResponseEntity<List<PublicHackathonResponse>> getHackathons(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) HackathonState state
    ) {
        List<Hackathon> hackathons;
        LocalDate today = LocalDate.now(clock);

        boolean hasName = name != null && !name.isBlank();

        if (hasName && state == null) {
            hackathons = hackathonCrudService.getHackathonByName(name);
        } else if (hasName) {
            hackathons = hackathonCrudService.getAllHackathonsByNameAndState(name, state);
        } else if (state != null) {
            hackathons = hackathonCrudService.getAllHackathonsByState(state);
        } else {
            hackathons = hackathonCrudService.getAllHackathons();
        }

        List<PublicHackathonResponse> response = hackathons.stream()
                .map(hackathon -> HackathonMapper.toPublic(hackathon, today))
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{hackathonId}")
    public ResponseEntity<?> getHackathon(
            @PathVariable UUID hackathonId,
            @RequestParam(name = "view", defaultValue = "full") String view
    ) {
        Hackathon hackathon = hackathonCrudService.getHackathonById(hackathonId);
        LocalDate today = LocalDate.now(clock);

        if ("public".equalsIgnoreCase(view)) {
            return ResponseEntity.ok(HackathonMapper.toPublic(hackathon, today));
        }
        if ("full".equalsIgnoreCase(view)) {
            FullHackathonResponse fullResponse = HackathonMapper.toFull(hackathon, today);
            return ResponseEntity.ok(fullResponse);
        }
        throw new IllegalArgumentException("view must be one of: public, full.");
    }

    @GetMapping("/{hackathonId}/teams")
    public ResponseEntity<List<TeamResponse>> getTeamsByHackathon(
            @PathVariable UUID hackathonId
    ) {
        return ResponseEntity.ok(
                teamParticipationService.getTeamsByHackathon(hackathonId)
                        .stream()
                        .map(TeamMapper::toResponse)
                        .toList()
        );
    }

    @GetMapping("/{hackathonId}/submissions")
    public ResponseEntity<List<SubmissionResponse>> getSubmissionsByHackathon(
            @PathVariable UUID hackathonId
    ) {
        return ResponseEntity.ok(
                teamParticipationService.getSubmissionsByHackathon(hackathonId)
                        .stream()
                        .map(SubmissionMapper::toResponse)
                        .toList()
        );
    }

    @PostMapping
    public ResponseEntity<Void> createHackathon(@Valid @RequestBody CreateHackathonRequest request) {
        Hackathon createdHackathon = hackathonCrudService.createHackathon(
                request.creatorId(),
                request.name(),
                request.description(),
                new ArrayList<>(),
                new ArrayList<>(),
                request.awardPrize(),
                null
        );

        if (hasTimelineInput(request.registrationDeadline(), request.submissionDeadline(), request.evaluationDeadline())) {
            HackathonTimeline timeline = new HackathonTimeline(
                    request.registrationDeadline(),
                    request.submissionDeadline(),
                    request.evaluationDeadline()
            );
            hackathonCrudService.changeHackathonTimeline(
                    createdHackathon.getId(),
                    timeline
            );
        }

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{hackathonId}")
                .buildAndExpand(createdHackathon.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @PatchMapping("/{hackathonId}")
    public ResponseEntity<Void> updateHackathon(
            @PathVariable UUID hackathonId,
            @Valid @RequestBody UpdateHackathonRequest request
    ) {
        if (request.description() != null && !request.description().isBlank()) {
            hackathonCrudService.changeDescription(hackathonId, request.description());
        }

        if (hasTimelineInput(request.registrationDeadline(), request.submissionDeadline(), request.evaluationDeadline())) {
            HackathonTimeline timeline = new HackathonTimeline(
                    request.registrationDeadline(),
                    request.submissionDeadline(),
                    request.evaluationDeadline()
            );
            hackathonCrudService.changeHackathonTimeline(
                    hackathonId,
                    timeline
            );
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{hackathonId}/state")
    public ResponseEntity<HackathonStateResponse> getHackathonState(@PathVariable UUID hackathonId) {
        return ResponseEntity.ok(new HackathonStateResponse(
                hackathonId,
                hackathonLifecycleService.determineCurrentState(hackathonId)
        ));
    }

    @PostMapping("/{hackathonId}/staff-assignments")
    public ResponseEntity<Void> addStaffAssignments(
            @PathVariable UUID hackathonId,
            @Valid @RequestBody AddStaffAssignmentsRequest request
    ) {
        hackathonStaffService.addStaffMembers(request.assignerId() ,hackathonId, HackathonMapper.toStaffMap(request.staffAssignments()));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{hackathonId}/conclusions")
    public ResponseEntity<Void> concludeHackathon(@PathVariable UUID hackathonId) {
        hackathonLifecycleService.concludeHackathon(hackathonId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{hackathonId}/staff-assignment")
    public ResponseEntity<Void> assignStaff(
            @PathVariable UUID hackathonId,
            @Valid @RequestBody AssignStaffRequest request
    ) {
        hackathonStaffService.assignStaffToHackathon(
                request.assignerId(),
                hackathonId,
                request.staffMemberId(),
                request.role()
        );

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{hackathonId}/winner")
    public ResponseEntity<Void> assignWinner(
            @PathVariable UUID hackathonId,
            @Valid @RequestBody AssignWinnerRequest request
    ) {
        hackathonLifecycleService.assignWinner(hackathonId, request.winnerTeamId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{hackathonId}/staff-assignments/{assignmentId}")
    public ResponseEntity<Void> deleteStaffAssignment(
            @PathVariable UUID hackathonId,
            @PathVariable UUID assignmentId
    ) {
        hackathonStaffService.deleteStaffAssignment(hackathonId, assignmentId);
        return ResponseEntity.noContent().build();
    }

    private static boolean hasTimelineInput(
            LocalDate registrationDeadline,
            LocalDate submissionDeadline,
            LocalDate evaluationDeadline
    ) {
        return registrationDeadline != null || submissionDeadline != null || evaluationDeadline != null;
    }
}

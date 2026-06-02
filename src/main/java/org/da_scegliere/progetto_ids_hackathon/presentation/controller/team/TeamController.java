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

package org.da_scegliere.progetto_ids_hackathon.presentation.controller.team;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.da_scegliere.progetto_ids_hackathon.application.services.TeamInvitationService;
import org.da_scegliere.progetto_ids_hackathon.application.services.TeamParticipationService;
import org.da_scegliere.progetto_ids_hackathon.application.services.TeamService;
import org.da_scegliere.progetto_ids_hackathon.application.services.views.team.TeamCreationRequestView;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Team;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.TeamParticipation;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.team.UpdateTeamRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.user.CreateTeamRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.team.TeamParticipationResponse;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.team.TeamResponse;
import org.da_scegliere.progetto_ids_hackathon.presentation.mapper.TeamMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/teams")
public class TeamController {

    private final TeamService teamService;
    private final TeamInvitationService teamInvitationService;
    private final TeamParticipationService teamParticipationService;

    @PostMapping
    public ResponseEntity<Void> createTeam(@Valid @RequestBody CreateTeamRequest request) {
        UUID creatorId = request.resolvedCreatorId();
        if (creatorId == null) {
            throw new IllegalArgumentException("creatorId must be provided.");
        }

        TeamCreationRequestView creationRequest = teamInvitationService.startTeamCreationRequest(
                creatorId,
                request.teamName(),
                request.resolvedInviteeIds()
        );

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/team-creation-requests/{requestId}")
                .buildAndExpand(creationRequest.id())
                .toUri();
        return ResponseEntity.accepted().location(location).build();
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<TeamResponse> getTeamById( @PathVariable UUID teamId) {
        return ResponseEntity.ok(TeamMapper.toResponse(teamService.getTeamById(teamId)));
    }

    @GetMapping
    public ResponseEntity<List<TeamResponse>> getTeams(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) UUID memberId
    ) {
        int activeFilters = (name != null ? 1 : 0) + (memberId != null ? 1 : 0);
        if (activeFilters > 1) {
            throw new IllegalArgumentException("Use only one among name or memberId.");
        }

        List<Team> teams;
        if (name != null) {
            teams = List.of(teamService.getTeamByName(name));
        } else if (memberId != null) {
            teams = List.of(teamService.getTeamByTeamMemberId(memberId));
        } else {
            teams = teamService.getTeams();
        }

        return ResponseEntity.ok(teams.stream().map(TeamMapper::toResponse).toList());
    }

    @GetMapping("/{teamId}/team-participations")
    public ResponseEntity<List<TeamParticipationResponse>> getTeamParticipations( @PathVariable UUID teamId){
        List<TeamParticipation> teamParticipations = teamParticipationService
                .getAllTeamParticipationsByTeamId(teamId);

        return ResponseEntity.ok(teamParticipations.stream().map(TeamParticipationController::toResponse).toList());
    }

    @PatchMapping("/{teamId}")
    public ResponseEntity<TeamResponse> updateTeam(
            @PathVariable UUID teamId,
            @Valid @RequestBody UpdateTeamRequest request
    ) {
        Team updatedTeam = teamService.changeTeamName(teamId, request.name());
        return ResponseEntity.ok(TeamMapper.toResponse(updatedTeam));
    }

    @PostMapping("/{teamId}/members/{userId}")
    public ResponseEntity<TeamResponse> addMemberToTeam(
            @PathVariable UUID teamId,
            @PathVariable UUID userId
    ) {
        Team updatedTeam = teamService.addMemberToTeam(teamId, userId);
        return ResponseEntity.ok(TeamMapper.toResponse(updatedTeam));
    }

    @DeleteMapping("/{teamId}/members/{userId}")
    public ResponseEntity<Void> removeMemberFromTeam(
            @PathVariable UUID teamId,
            @PathVariable UUID userId
    ) {
        teamService.removeMemberFromTeam(teamId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{teamId}")
    public ResponseEntity<Void> deleteTeam(@PathVariable UUID teamId) {
        teamService.deleteTeam(teamId);
        return ResponseEntity.noContent().build();
    }

}

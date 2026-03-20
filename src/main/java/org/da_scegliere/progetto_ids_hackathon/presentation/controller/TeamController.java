package org.da_scegliere.progetto_ids_hackathon.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.da_scegliere.progetto_ids_hackathon.application.services.TeamService;
import org.da_scegliere.progetto_ids_hackathon.application.services.UserService;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Team;
import org.da_scegliere.progetto_ids_hackathon.core.entities.user.User;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.UpdateTeamRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.user.CreateTeamRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.TeamDetailsResponse;
import org.da_scegliere.progetto_ids_hackathon.presentation.mapper.UserMapper;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/teams")
public class TeamController {

    private final TeamService teamService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<Void> createTeam(@Valid @RequestBody CreateTeamRequest request) {
        List<UUID> usersId = UserMapper.toUserList(request.teamMembers());
        List<User> users = new ArrayList<>();
        for (UUID uuid : usersId) {
            users.add(userService.getUserById(uuid));
        }

        Team createdTeam = teamService.createTeam(request.teamName(), users);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{teamId}")
                .buildAndExpand(createdTeam.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<TeamDetailsResponse> getTeamById(@PathVariable UUID teamId) {
        return ResponseEntity.ok(toResponse(teamService.getTeamById(teamId)));
    }

    @GetMapping
    public ResponseEntity<List<TeamDetailsResponse>> getTeams(
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

        return ResponseEntity.ok(teams.stream().map(TeamController::toResponse).toList());
    }

    @PatchMapping("/{teamId}")
    public ResponseEntity<TeamDetailsResponse> updateTeam(
            @PathVariable UUID teamId,
            @Valid @RequestBody UpdateTeamRequest request
    ) {
        Team updatedTeam = teamService.changeTeamName(teamId, request.name());
        return ResponseEntity.ok(toResponse(updatedTeam));
    }

    @PostMapping("/{teamId}/members/{userId}")
    public ResponseEntity<TeamDetailsResponse> addMemberToTeam(
            @PathVariable UUID teamId,
            @PathVariable UUID userId
    ) {
        Team updatedTeam = teamService.addMemberToTeam(teamId, userId);
        return ResponseEntity.ok(toResponse(updatedTeam));
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

    private static TeamDetailsResponse toResponse(Team team) {
        List<UUID> memberIds = team.getMembers() == null
                ? List.of()
                : team.getMembers().stream().map(User::getId).toList();
        return new TeamDetailsResponse(team.getId(), team.getName(), memberIds);
    }
}

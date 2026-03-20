package org.da_scegliere.progetto_ids_hackathon.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.da_scegliere.progetto_ids_hackathon.application.services.hackathon.HackathonCrudService;
import org.da_scegliere.progetto_ids_hackathon.application.services.hackathon.HackathonLifecycleService;
import org.da_scegliere.progetto_ids_hackathon.application.services.hackathon.HackathonStaffService;
import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.Hackathon;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.AddStaffAssignmentsRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.AssignWinnerRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.CreateHackathonRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.UpdateHackathonRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.FullHackathonResponse;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.HackathonStateResponse;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.PublicHackathonResponse;
import org.da_scegliere.progetto_ids_hackathon.presentation.mapper.HackathonMapper;
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

import java.net.URI;
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

    @GetMapping
    public ResponseEntity<List<PublicHackathonResponse>> getHackathons(
            @RequestParam(required = false) String name
    ) {
        List<Hackathon> hackathons = name == null || name.isBlank()
                ? hackathonCrudService.getAllHackathons()
                : List.of(hackathonCrudService.getHackathonByName(name));
        List<PublicHackathonResponse> response = hackathons.stream().map(HackathonMapper::toPublic).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{hackathonId}")
    public ResponseEntity<?> getHackathon(
            @PathVariable UUID hackathonId,
            @RequestParam(name = "view", defaultValue = "full") String view
    ) {
        Hackathon hackathon = hackathonCrudService.getHackathonById(hackathonId);

        if ("public".equalsIgnoreCase(view)) {
            return ResponseEntity.ok(HackathonMapper.toPublic(hackathon));
        }
        if ("full".equalsIgnoreCase(view)) {
            FullHackathonResponse fullResponse = HackathonMapper.toFull(hackathon);
            return ResponseEntity.ok(fullResponse);
        }
        throw new IllegalArgumentException("view must be one of: public, full.");
    }

    @PostMapping
    public ResponseEntity<Void> createHackathon(@Valid @RequestBody CreateHackathonRequest request) {
        Hackathon createdHackathon = hackathonCrudService.createHackathon(
                request.name(),
                request.description(),
                new ArrayList<>(),
                new ArrayList<>(),
                request.awardPrize(),
                null
        );

        if (request.registrationDeadline() != null
                || request.submissionDeadline() != null
                || request.evaluationDeadline() != null) {
            hackathonCrudService.changeHackathonDeadlines(
                    createdHackathon.getId(),
                    request.registrationDeadline(),
                    request.evaluationDeadline(),
                    request.submissionDeadline()
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

        if (request.registrationDeadline() != null
                || request.submissionDeadline() != null
                || request.evaluationDeadline() != null) {
            hackathonCrudService.changeHackathonDeadlines(
                    hackathonId,
                    request.registrationDeadline(),
                    request.evaluationDeadline(),
                    request.submissionDeadline()
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
        hackathonStaffService.addStaffMembers(hackathonId, HackathonMapper.toStaffMap(request.staffAssignments()));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{hackathonId}/conclusions")
    public ResponseEntity<Void> concludeHackathon(@PathVariable UUID hackathonId) {
        hackathonLifecycleService.concludeHackathon(hackathonId);
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
}

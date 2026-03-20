package org.da_scegliere.progetto_ids_hackathon.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.da_scegliere.progetto_ids_hackathon.application.services.TeamParticipationService;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Submission;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.TeamParticipation;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.CreateSubmissionRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.SubmissionResponse;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.TeamParticipationResponse;
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

    @GetMapping("/{participationId}")
    public ResponseEntity<TeamParticipationResponse> getTeamParticipation(@PathVariable UUID participationId) {
        TeamParticipation participation = teamParticipationService.getTeamParticipationById(participationId);
        return ResponseEntity.ok(toResponse(participation));
    }

    @GetMapping("/{participationId}/submissions")
    public ResponseEntity<List<SubmissionResponse>> getSubmissions(@PathVariable UUID participationId) {
        List<SubmissionResponse> responses = teamParticipationService.getSubmissionsByTeamParticipation(participationId)
                .stream()
                .map(SubmissionController::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
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

    private static TeamParticipationResponse toResponse(TeamParticipation participation) {
        UUID teamId = participation.getTeam() != null ? participation.getTeam().getId() : null;
        UUID hackathonId = participation.getHackathon() != null ? participation.getHackathon().getId() : null;

        return new TeamParticipationResponse(
                participation.getId(),
                participation.getNickname(),
                participation.getEntryDate(),
                teamId,
                hackathonId
        );
    }
}

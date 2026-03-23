package org.da_scegliere.progetto_ids_hackathon.presentation.controller.team;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.da_scegliere.progetto_ids_hackathon.application.services.TeamInvitationService;
import org.da_scegliere.progetto_ids_hackathon.application.services.views.team.TeamCreationRequestView;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.team.CancelTeamCreationRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.team.StartTeamCreationRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.team.TeamCreationRequestResponse;
import org.da_scegliere.progetto_ids_hackathon.presentation.mapper.TeamInvitationMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/api/v1/team-creation-requests")
public class TeamCreationRequestController {

    private final TeamInvitationService teamInvitationService;

    @PostMapping
    public ResponseEntity<Void> startTeamCreationRequest(@Valid @RequestBody StartTeamCreationRequest request) {
        TeamCreationRequestView createdRequest = teamInvitationService.startTeamCreationRequest(
                request.creatorId(),
                request.teamName(),
                request.inviteeIds()
        );

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{requestId}")
                .buildAndExpand(createdRequest.id())
                .toUri();
        return ResponseEntity.accepted().location(location).build();
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<TeamCreationRequestResponse> getTeamCreationRequest(@PathVariable UUID requestId) {
        TeamCreationRequestView request = teamInvitationService.getTeamCreationRequest(requestId);
        return ResponseEntity.ok(TeamInvitationMapper.toTeamCreationRequestResponse(request));
    }

    @GetMapping
    public ResponseEntity<List<TeamCreationRequestResponse>> getTeamCreationRequestsByCreator(
            @RequestParam UUID creatorId
    ) {
        List<TeamCreationRequestResponse> response = teamInvitationService.getRequestsByCreator(creatorId)
                .stream()
                .map(TeamInvitationMapper::toTeamCreationRequestResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{requestId}")
    public ResponseEntity<TeamCreationRequestResponse> cancelTeamCreationRequest(
            @PathVariable UUID requestId,
            @Valid @RequestBody CancelTeamCreationRequest request
    ) {
        TeamCreationRequestView cancelledRequest = teamInvitationService.cancelRequest(requestId, request.requesterId());
        return ResponseEntity.ok(TeamInvitationMapper.toTeamCreationRequestResponse(cancelledRequest));
    }
}

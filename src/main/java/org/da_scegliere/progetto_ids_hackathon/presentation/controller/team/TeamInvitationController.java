package org.da_scegliere.progetto_ids_hackathon.presentation.controller.team;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.da_scegliere.progetto_ids_hackathon.application.services.TeamInvitationService;
import org.da_scegliere.progetto_ids_hackathon.application.services.views.team.TeamInvitationView;
import org.da_scegliere.progetto_ids_hackathon.core.enums.state.team.TeamInvitationStatus;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.team.TeamInvitationActionRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.team.TeamInvitationResponse;
import org.da_scegliere.progetto_ids_hackathon.presentation.mapper.TeamInvitationMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/team-invitations")
public class TeamInvitationController {

    private final TeamInvitationService teamInvitationService;

    @GetMapping
    public ResponseEntity<List<TeamInvitationResponse>> getInvitations(
            @RequestParam UUID inviteeId,
            @RequestParam(required = false) TeamInvitationStatus status
    ) {
        List<TeamInvitationView> invitations = teamInvitationService.getInvitationsForInvitee(inviteeId, status);
        return ResponseEntity.ok(TeamInvitationMapper.toTeamInvitationResponseList(invitations));
    }

    @PostMapping("/{invitationId}/accept")
    public ResponseEntity<TeamInvitationResponse> acceptInvitation(
            @PathVariable UUID invitationId,
            @Valid @RequestBody TeamInvitationActionRequest request
    ) {
        TeamInvitationView invitation = teamInvitationService.acceptInvitation(invitationId, request.inviteeId());
        return ResponseEntity.ok(TeamInvitationMapper.toTeamInvitationResponse(invitation));
    }

    @PostMapping("/{invitationId}/reject")
    public ResponseEntity<TeamInvitationResponse> rejectInvitation(
            @PathVariable UUID invitationId,
            @Valid @RequestBody TeamInvitationActionRequest request
    ) {
        TeamInvitationView invitation = teamInvitationService.rejectInvitation(invitationId, request.inviteeId());
        return ResponseEntity.ok(TeamInvitationMapper.toTeamInvitationResponse(invitation));
    }
}

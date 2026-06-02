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
import org.da_scegliere.progetto_ids_hackathon.application.services.CalendarService;
import org.da_scegliere.progetto_ids_hackathon.application.services.SupportRequestService;
import org.da_scegliere.progetto_ids_hackathon.core.entities.staff.StaffAssignment;
import org.da_scegliere.progetto_ids_hackathon.core.entities.support.SupportRequest;
import org.da_scegliere.progetto_ids_hackathon.core.enums.state.support.SupportRequestState;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.supportRequest.CreateSupportRequestRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.supportRequest.MarkSupportRequestInProgressRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.supportRequest.SupportRequestResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
@RequestMapping("/api/v1/support-requests")
public class SupportRequestController {

    private final CalendarService calendarService;
    private final SupportRequestService supportRequestService;

    @PostMapping("/{requestId}/call-proposals")
    public ResponseEntity<Void> proposeCall(@PathVariable UUID requestId) {
        calendarService.proposeCall(requestId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<Void> createSupportRequest(@Valid @RequestBody CreateSupportRequestRequest request) {
        SupportRequest created = supportRequestService.createSupportRequest(
                request.dateSlot(),
                request.teamId(),
                request.staffAssignmentIds()
        );
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{requestId}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<SupportRequestResponse> getSupportRequest(@PathVariable UUID requestId) {
        return ResponseEntity.ok(toResponse(supportRequestService.getSupportRequestById(requestId)));
    }

    @GetMapping
    public ResponseEntity<List<SupportRequestResponse>> getSupportRequests(
            @RequestParam(required = false) UUID teamId,
            @RequestParam(required = false) SupportRequestState state
    ) {
        List<SupportRequest> requests = teamId == null
                ? supportRequestService.getAllSupportRequest()
                : supportRequestService.getSupportRequestByTeam(teamId);

        if (state != null) {
            requests = requests.stream().filter(request -> request.getState() == state).toList();
        }

        return ResponseEntity.ok(requests.stream().map(SupportRequestController::toResponse).toList());
    }

    @PutMapping("/{requestId}/in-progress")
    public ResponseEntity<SupportRequestResponse> markInProgress(
            @PathVariable UUID requestId,
            @Valid @RequestBody MarkSupportRequestInProgressRequest request
    ) {
        SupportRequest updated = supportRequestService.markInProgress(requestId, request.acceptingMentorAssignmentId());
        return ResponseEntity.ok(toResponse(updated));
    }

    @PutMapping("/{requestId}/resolved")
    public ResponseEntity<SupportRequestResponse> resolveSupportRequest(@PathVariable UUID requestId) {
        SupportRequest updated = supportRequestService.resolveRequest(requestId);
        return ResponseEntity.ok(toResponse(updated));
    }

    @PutMapping("/{requestId}/rejected")
    public ResponseEntity<SupportRequestResponse> rejectSupportRequest(@PathVariable UUID requestId) {
        SupportRequest updated = supportRequestService.rejectRequest(requestId);
        return ResponseEntity.ok(toResponse(updated));
    }

    @DeleteMapping("/{requestId}")
    public ResponseEntity<Void> deleteSupportRequest(@PathVariable UUID requestId) {
        supportRequestService.deleteSupportRequest(requestId);
        return ResponseEntity.noContent().build();
    }

    private static SupportRequestResponse toResponse(SupportRequest supportRequest) {
        UUID teamId = supportRequest.getSendingTeam() != null ? supportRequest.getSendingTeam().getId() : null;
        UUID acceptingMentorId = supportRequest.getAcceptingMentor() != null
                ? supportRequest.getAcceptingMentor().getId()
                : null;

        List<UUID> selectedMentors = supportRequest.getSelectedMentors() == null
                ? List.of()
                : supportRequest.getSelectedMentors().stream().map(StaffAssignment::getId).toList();

        return new SupportRequestResponse(
                supportRequest.getId(),
                supportRequest.getRequestedCallDate(),
                supportRequest.getState(),
                teamId,
                acceptingMentorId,
                selectedMentors
        );
    }
}

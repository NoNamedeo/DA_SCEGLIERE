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

package org.da_scegliere.progetto_ids_hackathon.presentation.controller.staff;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.da_scegliere.progetto_ids_hackathon.application.services.StaffService;
import org.da_scegliere.progetto_ids_hackathon.application.services.moderation.ModerationReportService;
import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.Hackathon;
import org.da_scegliere.progetto_ids_hackathon.core.entities.moderation.ModerationReport;
import org.da_scegliere.progetto_ids_hackathon.core.entities.staff.StaffMember;
import org.da_scegliere.progetto_ids_hackathon.core.enums.StaffRole;
import org.da_scegliere.progetto_ids_hackathon.core.enums.state.report.UserReportState;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.staff.CreateStaffMemberRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.staff.UpdateStaffMemberRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.teamParticipation.DisqualifyTeamParticipationFromReportRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.hackathon.HackathonSummaryResponse;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.moderation.ModerationReportResponse;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.staff.StaffMemberResponse;
import org.da_scegliere.progetto_ids_hackathon.presentation.mapper.ModerationReportMapper;
import org.da_scegliere.progetto_ids_hackathon.presentation.mapper.StaffMapper;
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
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/staff-members")
public class StaffController {

    private final StaffService staffService;
    private final ModerationReportService moderationReportService;

    @PostMapping
    public ResponseEntity<Void> createStaffMember(@Valid @RequestBody CreateStaffMemberRequest request) {
        StaffMember createdStaffMember = staffService.createStaffMember(
                request.name(),
                request.age(),
                request.email()
        );

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{staffMemberId}")
                .buildAndExpand(createdStaffMember.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{staffMemberId}")
    public ResponseEntity<StaffMemberResponse> getStaffMember(@PathVariable UUID staffMemberId) {
        return ResponseEntity.ok(StaffMapper.toResponse(staffService.getStaffMemberById(staffMemberId)));
    }

    @GetMapping
    public ResponseEntity<List<StaffMemberResponse>> getStaffMembers(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) StaffRole role,
            @RequestParam(required = false) UUID hackathonId
    ) {
        List<StaffMember> staffMembers;
        if (email != null) {
            staffMembers = List.of(staffService.getStaffMemberByEmail(email));
        } else if (hackathonId != null && role != null) {
            staffMembers = staffService.getStaffMembersByHackathonAndRole(hackathonId, role);
        } else if (hackathonId != null) {
            staffMembers = staffService.getStaffMembersByHackathon(hackathonId);
        } else if (role != null) {
            staffMembers = staffService.getStaffMembersByRole(role);
        } else {
            staffMembers = staffService.getAllStaffMembers();
        }

        return ResponseEntity.ok(staffMembers.stream().map(StaffMapper::toResponse).toList());
    }

    @PatchMapping("/{staffMemberId}")
    public ResponseEntity<StaffMemberResponse> updateStaffMember(
            @PathVariable UUID staffMemberId,
            @Valid @RequestBody UpdateStaffMemberRequest request
    ) {
        StaffMember updatedStaffMember = staffService.changeStaffMemberName(staffMemberId, request.name());
        return ResponseEntity.ok(StaffMapper.toResponse(updatedStaffMember));
    }

    @GetMapping("/{staffMemberId}/managed-hackathons")
    public ResponseEntity<List<HackathonSummaryResponse>> getManagedHackathons(
            @PathVariable UUID staffMemberId,
            @RequestParam(required = false) StaffRole role
    ) {
        List<Hackathon> hackathons = role == null
                ? staffService.getHackathonsManagedByStaffMember(staffMemberId)
                : staffService.getHackathonsManagedByStaffMemberAndRole(staffMemberId, role);
        return ResponseEntity.ok(hackathons.stream().map(StaffMapper::toHackathonSummary).toList());
    }

    @GetMapping("/{staffMemberId}/managed-hackathons/grouped-by-role")
    public ResponseEntity<Map<StaffRole, List<HackathonSummaryResponse>>> getManagedHackathonsGroupedByRole(
            @PathVariable UUID staffMemberId
    ) {
        Map<StaffRole, List<HackathonSummaryResponse>> response = StaffMapper.toGroupedResponse(
                staffService.getHackathonsManagedByStaffMemberPerRole(staffMemberId)
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/managed-hackathons/grouped-by-role")
    public ResponseEntity<Map<StaffRole, List<HackathonSummaryResponse>>> getManagedHackathonsGroupedByRole() {
        Map<StaffRole, List<HackathonSummaryResponse>> response =
                StaffMapper.toGroupedResponse(staffService.getHackathonsManagedPerRole());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{staffMemberId}/team-reports/{reportId}/disqualifications")
    public ResponseEntity<Void> disqualifyTeamParticipationFromReport(
            @PathVariable UUID staffMemberId,
            @PathVariable UUID reportId,
            @Valid @RequestBody DisqualifyTeamParticipationFromReportRequest request
    ) {
        staffService.disqualifyTeamParticipationFromReport(
                staffMemberId,
                reportId,
                request.disqualificationReason(),
                request.reportResolutionNotes()
        );
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{staffMemberId}/hackathons/{hackathonId}/team-reports")
    public ResponseEntity<List<ModerationReportResponse>> getTeamParticipationReportsByHackathon(
            @PathVariable UUID staffMemberId,
            @PathVariable UUID hackathonId
    ) {
        staffService.getStaffMemberById(staffMemberId);
        return ResponseEntity.ok(
                moderationReportService.getReportsByHackathonId(hackathonId).stream()
                        .map(ModerationReportMapper::toResponse)
                        .toList()
        );
    }

    @DeleteMapping("/{staffMemberId}")
    public ResponseEntity<Void> deleteStaffMember(@PathVariable UUID staffMemberId) {
        staffService.deleteStaffMember(staffMemberId);
        return ResponseEntity.noContent().build();
    }

}

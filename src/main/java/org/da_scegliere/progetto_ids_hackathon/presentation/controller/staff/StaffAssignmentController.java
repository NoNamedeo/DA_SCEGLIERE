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

package org.da_scegliere.progetto_ids_hackathon.presentation.controller.staff;

import lombok.RequiredArgsConstructor;
import org.da_scegliere.progetto_ids_hackathon.application.services.StaffService;
import org.da_scegliere.progetto_ids_hackathon.core.entities.staff.StaffAssignment;
import org.da_scegliere.progetto_ids_hackathon.core.enums.StaffRole;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.staff.StaffAssignmentDetailsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/staff-assignments")
public class StaffAssignmentController {

    private final StaffService staffService;

    @GetMapping
    public ResponseEntity<List<StaffAssignmentDetailsResponse>> getStaffAssignments(
            @RequestParam(required = false) UUID staffMemberId,
            @RequestParam(required = false) UUID hackathonId,
            @RequestParam(required = false) StaffRole role
    ) {
        List<StaffAssignment> assignments;
        if (staffMemberId != null && hackathonId != null && role != null) {
            assignments = staffService.getStaffAssignmentsByStaffMemberAndRole(staffMemberId, role).stream()
                    .filter(assignment -> assignment.getHackathon() != null)
                    .filter(assignment -> hackathonId.equals(assignment.getHackathon().getId()))
                    .toList();
        } else if (staffMemberId != null && role != null) {
            assignments = staffService.getStaffAssignmentsByStaffMemberAndRole(staffMemberId, role);
        } else if (staffMemberId != null && hackathonId != null) {
            assignments = staffService.getStaffAssignmentsByStaffMemberAndHackathon(staffMemberId, hackathonId);
        } else if (staffMemberId != null) {
            assignments = staffService.getStaffAssignmentsByStaffMember(staffMemberId);
        } else if (hackathonId != null && role != null) {
            assignments = staffService.getStaffAssignmentsByHackathonAndRole(hackathonId, role);
        } else if (hackathonId != null) {
            assignments = staffService.getStaffAssignmentsByHackathon(hackathonId);
        } else if (role != null) {
            assignments = staffService.getStaffAssignmentsByRole(role);
        } else {
            assignments = staffService.getAllStaffAssignments();
        }

        return ResponseEntity.ok(assignments.stream().map(StaffAssignmentController::toResponse).toList());
    }

    private static StaffAssignmentDetailsResponse toResponse(StaffAssignment assignment) {
        UUID staffMemberId = assignment.getStaffMember() != null ? assignment.getStaffMember().getId() : null;
        UUID hackathonId = assignment.getHackathon() != null ? assignment.getHackathon().getId() : null;
        return new StaffAssignmentDetailsResponse(
                assignment.getId(),
                staffMemberId,
                hackathonId,
                assignment.getStaffRole(),
                assignment.getAssignmentDate()
        );
    }
}

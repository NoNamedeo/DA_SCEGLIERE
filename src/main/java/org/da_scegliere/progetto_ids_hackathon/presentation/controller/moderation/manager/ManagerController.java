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

package org.da_scegliere.progetto_ids_hackathon.presentation.controller.moderation.manager;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.da_scegliere.progetto_ids_hackathon.application.services.moderation.manager.ManagerService;
import org.da_scegliere.progetto_ids_hackathon.core.entities.moderation.BugReport;
import org.da_scegliere.progetto_ids_hackathon.core.entities.moderation.ModerationReport;
import org.da_scegliere.progetto_ids_hackathon.core.entities.moderation.StaffReport;
import org.da_scegliere.progetto_ids_hackathon.core.entities.moderation.TeamParticipationReport;
import org.da_scegliere.progetto_ids_hackathon.core.entities.moderation.UserReport;
import org.da_scegliere.progetto_ids_hackathon.core.enums.state.report.UserReportState;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.moderation.ModerationActionRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.user.SuspendUserFromReportRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.user.SuspendUserRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.moderation.ModerationReportResponse;
import org.da_scegliere.progetto_ids_hackathon.presentation.mapper.ModerationReportMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/managers")
public class ManagerController {

    private final ManagerService managerService;

    @GetMapping("/{managerId}/moderation-reports")
    public ResponseEntity<List<ModerationReportResponse>> getManagerModerationReports(
            @PathVariable UUID managerId,
            @RequestParam(required = false) UserReportState state,
            @RequestParam(defaultValue = "ALL") String targetType
    ) {
        String normalizedTargetType = targetType == null ? "ALL" : targetType.trim().toUpperCase();
        List<? extends ModerationReport> reports = switch (normalizedTargetType) {
            case "ALL" -> resolveAllReports(managerId, state);
            case "USER" -> resolveUserReports(managerId, state);
            case "STAFF" -> resolveStaffReports(managerId, state);
            case "BUG" -> resolveBugReports(managerId, state);
            case "TEAM" -> resolveTeamParticipationReports(managerId, state);
            default -> throw new IllegalArgumentException("targetType must be one of: ALL, USER, STAFF, BUG, TEAM.");
        };

        return ResponseEntity.ok(reports.stream().map(ModerationReportMapper::toResponse).toList());
    }

    @PostMapping("/{managerId}/users/{userId}/suspensions")
    public ResponseEntity<Void> suspendUser(
            @PathVariable UUID managerId,
            @PathVariable UUID userId,
            @Valid @RequestBody SuspendUserRequest request
    ) {
        managerService.suspendUser(managerId, userId, request.reason());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{managerId}/user-reports/{reportId}/suspensions")
    public ResponseEntity<Void> suspendUserFromReport(
            @PathVariable UUID managerId,
            @PathVariable UUID reportId,
            @Valid @RequestBody SuspendUserFromReportRequest request
    ) {
        managerService.suspendUserFromReport(
                managerId,
                reportId,
                request.suspensionReason(),
                request.reportResolutionNotes()
        );
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{managerId}/users/{userId}/reinstatements")
    public ResponseEntity<Void> reinstateUser(
            @PathVariable UUID managerId,
            @PathVariable UUID userId,
            @Valid @RequestBody ModerationActionRequest request
    ) {
        managerService.reinstateUser(managerId, userId, request.reason());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{managerId}/users/{userId}/revocations")
    public ResponseEntity<Void> revokeUser(
            @PathVariable UUID managerId,
            @PathVariable UUID userId,
            @Valid @RequestBody ModerationActionRequest request
    ) {
        managerService.revokeAccount(managerId, userId, request.reason());
        return ResponseEntity.noContent().build();
    }

    private List<ModerationReport> resolveAllReports(UUID managerId, UserReportState state) {
        if (state == null) {
            return managerService.getAllReports(managerId);
        }
        if (state == UserReportState.OPEN) {
            return managerService.getOpenReports(managerId);
        }
        return managerService.getAllReports(managerId).stream()
                .filter(report -> report.getState() == state)
                .toList();
    }

    private List<UserReport> resolveUserReports(UUID managerId, UserReportState state) {
        if (state == null) {
            return managerService.getAllUserReports(managerId);
        }
        if (state == UserReportState.OPEN) {
            return managerService.getOpenUserReports(managerId);
        }
        return managerService.getAllUserReports(managerId).stream()
                .filter(report -> report.getState() == state)
                .toList();
    }

    private List<StaffReport> resolveStaffReports(UUID managerId, UserReportState state) {
        List<ModerationReport> allReports = resolveAllReports(managerId, state);
        return allReports.stream()
                .filter(StaffReport.class::isInstance)
                .map(StaffReport.class::cast)
                .toList();
    }

    private List<BugReport> resolveBugReports(UUID managerId, UserReportState state) {
        List<ModerationReport> allReports = resolveAllReports(managerId, state);
        return allReports.stream()
                .filter(BugReport.class::isInstance)
                .map(BugReport.class::cast)
                .toList();
    }

    private List<TeamParticipationReport> resolveTeamParticipationReports(UUID managerId, UserReportState state) {
        List<ModerationReport> allReports = resolveAllReports(managerId, state);
        return allReports.stream()
                .filter(TeamParticipationReport.class::isInstance)
                .map(TeamParticipationReport.class::cast)
                .toList();
    }
}

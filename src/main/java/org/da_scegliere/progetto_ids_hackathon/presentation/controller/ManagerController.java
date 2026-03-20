package org.da_scegliere.progetto_ids_hackathon.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.da_scegliere.progetto_ids_hackathon.application.services.ManagerService;
import org.da_scegliere.progetto_ids_hackathon.core.entities.moderation.ModerationReport;
import org.da_scegliere.progetto_ids_hackathon.core.entities.moderation.StaffReport;
import org.da_scegliere.progetto_ids_hackathon.core.entities.moderation.UserReport;
import org.da_scegliere.progetto_ids_hackathon.core.enums.state.report.UserReportState;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.ModerationActionRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.SuspendUserFromReportRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.user.SuspendUserRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.ModerationReportResponse;
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
            default -> throw new IllegalArgumentException("targetType must be one of: ALL, USER, STAFF.");
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
}

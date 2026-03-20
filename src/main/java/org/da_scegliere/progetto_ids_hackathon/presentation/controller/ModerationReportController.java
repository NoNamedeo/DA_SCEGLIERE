package org.da_scegliere.progetto_ids_hackathon.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.da_scegliere.progetto_ids_hackathon.application.services.ModerationReportService;
import org.da_scegliere.progetto_ids_hackathon.core.entities.moderation.ModerationReport;
import org.da_scegliere.progetto_ids_hackathon.core.entities.moderation.StaffReport;
import org.da_scegliere.progetto_ids_hackathon.core.entities.moderation.UserReport;
import org.da_scegliere.progetto_ids_hackathon.core.enums.state.report.UserReportState;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.CreateStaffModerationReportRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.CreateUserModerationReportRequest;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/moderation-reports")
public class ModerationReportController {

    private final ModerationReportService moderationReportService;

    @PostMapping("/users")
    public ResponseEntity<Void> createUserReport(@Valid @RequestBody CreateUserModerationReportRequest request) {
        UserReport created = moderationReportService.createUserReport(
                request.reporterId(),
                request.reporterType(),
                request.reportedUserId(),
                request.title(),
                request.description()
        );
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/moderation-reports/{reportId}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @PostMapping("/staff")
    public ResponseEntity<Void> createStaffReport(@Valid @RequestBody CreateStaffModerationReportRequest request) {
        StaffReport created = moderationReportService.createStaffReport(
                request.reporterId(),
                request.reporterType(),
                request.reportedStaffMemberId(),
                request.title(),
                request.description()
        );
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/moderation-reports/{reportId}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping
    public ResponseEntity<List<ModerationReportResponse>> getReports(
            @RequestParam(required = false) UserReportState state,
            @RequestParam(required = false) String targetType
    ) {
        List<? extends ModerationReport> reports = resolveReportList(state, targetType);
        return ResponseEntity.ok(reports.stream().map(ModerationReportMapper::toResponse).toList());
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<ModerationReportResponse> getReportById(@PathVariable UUID reportId) {
        return ResponseEntity.ok(ModerationReportMapper.toResponse(moderationReportService.getReportById(reportId)));
    }

    private List<? extends ModerationReport> resolveReportList(UserReportState state, String targetType) {
        if (targetType == null || targetType.isBlank()) {
            if (state == UserReportState.OPEN) {
                return moderationReportService.getOpenReports();
            }
            List<ModerationReport> reports = moderationReportService.getAllReports();
            return state == null ? reports : reports.stream().filter(report -> report.getState() == state).toList();
        }

        String normalizedTargetType = targetType.trim().toUpperCase();
        return switch (normalizedTargetType) {
            case "USER" -> {
                List<UserReport> reports = moderationReportService.getAllUserReports();
                yield state == null ? reports : reports.stream().filter(report -> report.getState() == state).toList();
            }
            case "STAFF" -> {
                List<StaffReport> reports = moderationReportService.getAllStaffReports();
                yield state == null ? reports : reports.stream().filter(report -> report.getState() == state).toList();
            }
            default -> throw new IllegalArgumentException("targetType must be one of: USER, STAFF.");
        };
    }

}

package org.da_scegliere.progetto_ids_hackathon.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.da_scegliere.progetto_ids_hackathon.application.services.StaffService;
import org.da_scegliere.progetto_ids_hackathon.core.entities.staff.StaffAssignment;
import org.da_scegliere.progetto_ids_hackathon.core.enums.StaffRole;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.StaffAssignmentDetailsResponse;
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

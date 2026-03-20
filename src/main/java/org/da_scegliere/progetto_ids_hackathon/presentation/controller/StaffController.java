package org.da_scegliere.progetto_ids_hackathon.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.da_scegliere.progetto_ids_hackathon.application.services.StaffService;
import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.Hackathon;
import org.da_scegliere.progetto_ids_hackathon.core.entities.staff.StaffMember;
import org.da_scegliere.progetto_ids_hackathon.core.enums.StaffRole;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.CreateStaffMemberRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.UpdateStaffMemberRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.HackathonSummaryResponse;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.StaffMemberResponse;
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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/staff-members")
public class StaffController {

    private final StaffService staffService;

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
        return ResponseEntity.ok(toResponse(staffService.getStaffMemberById(staffMemberId)));
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

        return ResponseEntity.ok(staffMembers.stream().map(StaffController::toResponse).toList());
    }

    @PatchMapping("/{staffMemberId}")
    public ResponseEntity<StaffMemberResponse> updateStaffMember(
            @PathVariable UUID staffMemberId,
            @Valid @RequestBody UpdateStaffMemberRequest request
    ) {
        StaffMember updatedStaffMember = staffService.changeStaffMemberName(staffMemberId, request.name());
        return ResponseEntity.ok(toResponse(updatedStaffMember));
    }

    @GetMapping("/{staffMemberId}/managed-hackathons")
    public ResponseEntity<List<HackathonSummaryResponse>> getManagedHackathons(
            @PathVariable UUID staffMemberId,
            @RequestParam(required = false) StaffRole role
    ) {
        List<Hackathon> hackathons = role == null
                ? staffService.getHackathonsManagedByStaffMember(staffMemberId)
                : staffService.getHackathonsManagedByStaffMemberAndRole(staffMemberId, role);
        return ResponseEntity.ok(hackathons.stream().map(StaffController::toHackathonSummary).toList());
    }

    @GetMapping("/{staffMemberId}/managed-hackathons/grouped-by-role")
    public ResponseEntity<Map<StaffRole, List<HackathonSummaryResponse>>> getManagedHackathonsGroupedByRole(
            @PathVariable UUID staffMemberId
    ) {
        Map<StaffRole, List<HackathonSummaryResponse>> response = toGroupedResponse(
                staffService.getHackathonsManagedByStaffMemberPerRole(staffMemberId)
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/managed-hackathons/grouped-by-role")
    public ResponseEntity<Map<StaffRole, List<HackathonSummaryResponse>>> getManagedHackathonsGroupedByRole() {
        Map<StaffRole, List<HackathonSummaryResponse>> response =
                toGroupedResponse(staffService.getHackathonsManagedPerRole());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{staffMemberId}")
    public ResponseEntity<Void> deleteStaffMember(@PathVariable UUID staffMemberId) {
        staffService.deleteStaffMember(staffMemberId);
        return ResponseEntity.noContent().build();
    }

    private static StaffMemberResponse toResponse(StaffMember staffMember) {
        return new StaffMemberResponse(
                staffMember.getId(),
                staffMember.getName(),
                staffMember.getAge(),
                staffMember.getEmail(),
                staffMember.getAccountStatus()
        );
    }

    private static HackathonSummaryResponse toHackathonSummary(Hackathon hackathon) {
        return new HackathonSummaryResponse(
                hackathon.getId(),
                hackathon.getName(),
                hackathon.getDescription(),
                hackathon.getRegistrationDeadline(),
                hackathon.getSubmissionDeadline(),
                hackathon.getEvaluationDeadline()
        );
    }

    private static Map<StaffRole, List<HackathonSummaryResponse>> toGroupedResponse(
            Map<StaffRole, List<Hackathon>> groupedHackathons
    ) {
        Map<StaffRole, List<HackathonSummaryResponse>> response = new EnumMap<>(StaffRole.class);
        for (Map.Entry<StaffRole, List<Hackathon>> entry : groupedHackathons.entrySet()) {
            response.put(entry.getKey(), entry.getValue().stream().map(StaffController::toHackathonSummary).toList());
        }
        return response;
    }
}

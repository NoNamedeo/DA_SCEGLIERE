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

package org.da_scegliere.progetto_ids_hackathon.application.services.hackathon;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.da_scegliere.progetto_ids_hackathon.application.ports.events.DomainEventPublisher;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IStaffAssignmentRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IStaffMemberRepository;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.hackathon.HackathonNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.hackathon.InvalidHackathonStateOperationException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.staff.StaffAssignmentConflictException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.staff.StaffMemberNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.Hackathon;
import org.da_scegliere.progetto_ids_hackathon.core.entities.staff.StaffAssignment;
import org.da_scegliere.progetto_ids_hackathon.core.entities.staff.StaffMember;
import org.da_scegliere.progetto_ids_hackathon.core.enums.StaffRole;
import org.da_scegliere.progetto_ids_hackathon.core.enums.state.hackathon.HackathonState;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.management.relation.InvalidRoleValueException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Application service for hackathon staff assignment use cases.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Resolve staff members and orchestrate assignment/removal on hackathon aggregates.</li>
 *     <li>Enforce lifecycle constraints for staff management operations.</li>
 *     <li>Delegate duplicate and ownership checks to domain methods.</li>
 * </ul>
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class HackathonStaffService {

    private final HackathonCrudService hackathonCrudService;
    private final IStaffMemberRepository staffMemberRepository;
    private final IStaffAssignmentRepository staffAssignmentRepository;
    private final DomainEventPublisher domainEventPublisher;
    private final Clock clock;

    /**
     * Assigns multiple staff members to a hackathon.
     *
     * @param assignerId assigner of the staff member.
     * @param hackathonId target hackathon identifier.
     * @param staffMembersIdMap map of staff member id to role.
     * @return updated hackathon aggregate.
     * @throws IllegalArgumentException when input map is null/empty/invalid, staff members are missing,
     *         or duplicate/domain assignment constraints are violated.
     * @throws org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.hackathon.HackathonNotFoundException
     *         when hackathon does not exist.
     * @throws InvalidHackathonStateOperationException when staff management is not allowed in current state.
     */
    @Transactional
    public Hackathon addStaffMembers(UUID assignerId,
                                     UUID hackathonId,
                                     Map<UUID, StaffRole> staffMembersIdMap){
        log.info("Adding staff members to hackathon {}", hackathonId);

        if (staffMembersIdMap == null) {
            throw new IllegalArgumentException("staffMembersIdMap must not be null.");
        }
        if (staffMembersIdMap.isEmpty()) {
            throw new IllegalArgumentException("staffMembersIdMap must not be empty.");
        }

        Hackathon hackathon = prepareAssignment(assignerId, hackathonId);

        for (Map.Entry<UUID, StaffRole> entry : staffMembersIdMap.entrySet()) {
            UUID staffId = entry.getKey();
            StaffRole role = entry.getValue();

            if (staffId == null || role == null) {
                throw new IllegalArgumentException("staffMembersIdMap must contain only non-null keys and values.");
            }

            StaffMember member = staffMemberRepository.findById(staffId)
                    .orElseThrow(() -> new StaffMemberNotFoundException(staffId));

            createAndPersistAssignment(hackathon, member, role);
        }

        log.info("Added staff members to hackathon {}", hackathonId);
        return hackathon;
    }

    /**
     * Assigns a {@link StaffMember} to a {@link Hackathon} with a specific {@link StaffRole}.
     * <p>
     * @param assignerId assigner of the staff member.
     * @param hackathonId identifier of the hackathon to which the staff member will be assigned
     * @param staffMemberId identifier of the staff member to assign
     * @param role role to assign within the hackathon (e.g. JUDGE, ORGANIZER, etc.)
     * @return the persisted {@link StaffAssignment}
     *
     * @throws NullPointerException if any input parameter is {@code null}
     * @throws HackathonNotFoundException if the hackathon cannot be found staff member
     * @throws StaffMemberNotFoundException if the staff member  cannot be found
     *
     * @implNote
     * This method is transactional and ensures atomic consistency between:
     * hackathon aggregate state, assignment persistence, and notification creation.
     */
    @Transactional
    public StaffAssignment assignStaffToHackathon(
            UUID assignerId,
            UUID hackathonId,
            UUID staffMemberId,
            StaffRole role
    ){
        Objects.requireNonNull(role, "role must not be null");

        log.info("Assigning staffMember={} to hackathon={} with role={}",
                staffMemberId, hackathonId, role);

        Hackathon hackathon = prepareAssignment(staffMemberId, hackathonId);

        StaffMember staffMember = staffMemberRepository.findById(staffMemberId)
                .orElseThrow(() -> new StaffMemberNotFoundException(staffMemberId));

        return createAndPersistAssignment(hackathon, staffMember, role);
    }

    /**
     * Removes multiple staff members from a hackathon.
     *
     * @param hackathonId target hackathon identifier.
     * @param staffMembersId list of staff member identifiers to remove.
     * @return updated hackathon aggregate.
     * @throws IllegalArgumentException when input is null/empty/invalid or when any member
     *         is not currently assigned to the hackathon.
     * @throws org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.hackathon.HackathonNotFoundException
     *         when hackathon does not exist.
     * @throws InvalidHackathonStateOperationException when staff management is not allowed in current state.
     */
    @Transactional
    public Hackathon deleteStaffMembers(UUID hackathonId, List<UUID> staffMembersId) {
        log.info("Deleting staff members from hackathon {}", hackathonId);

        if (staffMembersId == null) {
            throw new IllegalArgumentException("staffMembersId must not be null.");
        }
        if (staffMembersId.isEmpty()) {
            throw new IllegalArgumentException("staffMembersId must not be empty.");
        }
        if (staffMembersId.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("staffMembersId must not contain null values.");
        }

        Hackathon hackathon = hackathonCrudService.getHackathonById(hackathonId);
        validateStaffManagementState(hackathon, LocalDate.now(clock));

        Set<UUID> idsToDelete = new HashSet<>(staffMembersId);
        int removedCount = hackathon.removeStaffAssignmentsByStaffMemberIds(idsToDelete);
        if (removedCount != idsToDelete.size()) {
            throw new IllegalArgumentException("One or more staff members are not assigned to this hackathon.");
        }

        log.info("Deleted staff members from hackathon {}", hackathonId);
        return hackathon;
    }

    /**
     * Deletes one staff assignment by assignment identifier within a hackathon.
     *
     * @param hackathonId target hackathon identifier.
     * @param assignmentId assignment identifier.
     * @return updated hackathon aggregate.
     */
    @Transactional
    public Hackathon deleteStaffAssignment(UUID hackathonId, UUID assignmentId) {
        if (assignmentId == null) {
            throw new IllegalArgumentException("assignmentId must not be null.");
        }
        StaffAssignment assignment = staffAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Staff assignment not found: " + assignmentId + "."));

        UUID assignmentHackathonId = assignment.getHackathon() != null ? assignment.getHackathon().getId() : null;
        if (!Objects.equals(assignmentHackathonId, hackathonId)) {
            throw new IllegalArgumentException("Staff assignment does not belong to the specified hackathon.");
        }

        UUID staffMemberId = assignment.getStaffMember() != null ? assignment.getStaffMember().getId() : null;
        if (staffMemberId == null) {
            throw new IllegalArgumentException("Staff assignment is not linked to a valid staff member.");
        }
        return deleteStaffMembers(hackathonId, List.of(staffMemberId));
    }

    private Hackathon prepareAssignment(UUID assignerId, UUID hackathonId) {
        Hackathon hackathon = hackathonCrudService.getHackathonById(hackathonId);
        validateStaffManagementState(hackathon, LocalDate.now(clock));
        validateAssignerRole(assignerId, hackathonId);
        return hackathon;
    }

    private static void validateStaffManagementState(Hackathon hackathon, LocalDate referenceDate) {
        HackathonState hackathonState = hackathon.getHackathonStateAt(referenceDate);
        if (!(hackathonState == HackathonState.REGISTRATION || hackathonState == HackathonState.ONGOING)) {
            throw new InvalidHackathonStateOperationException(hackathonState, "Staff management");
        }
    }

    private StaffAssignment createAndPersistAssignment(
            Hackathon hackathon,
            StaffMember member,
            StaffRole role
    ) {
        StaffAssignment assignment = new StaffAssignment(
                LocalDate.now(clock),
                role,
                member,
                hackathon
        );

        try {
            hackathon.addStaffAssignment(assignment);
        } catch (IllegalStateException ex) {
            throw new StaffAssignmentConflictException(ex.getMessage());
        }

        StaffAssignment saved = staffAssignmentRepository.save(assignment);
        domainEventPublisher.publish(saved.toAssignedEvent());

        return saved;
    }

    private void validateAssignerRole( UUID assignerId, UUID  hackathonId )  {
        if(assignerId == null) {
            throw new IllegalArgumentException("assignerId must not be null.");
        }
        StaffMember assigner = staffMemberRepository.findById(assignerId)
                .orElseThrow(() -> new StaffMemberNotFoundException(assignerId));

        boolean isOrganizer = assigner.getStaffAssignmentList()
                .stream()
                .anyMatch(assignment ->
                        assignment.getStaffRole() == StaffRole.ORGANIZER &&
                                Objects.equals(assignment.getHackathon().getId(), hackathonId)
                );

        if (!isOrganizer) {
            throw new StaffAssignmentConflictException(
                    "Assigner must be an organizer of hackathon " + hackathonId
            );
        }
    }
}

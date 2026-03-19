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
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IStaffMemberRepository;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.hackathon.InvalidHackathonStateOperationException;
import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.Hackathon;
import org.da_scegliere.progetto_ids_hackathon.core.entities.staff.StaffAssignment;
import org.da_scegliere.progetto_ids_hackathon.core.entities.staff.StaffMember;
import org.da_scegliere.progetto_ids_hackathon.core.enums.StaffRole;
import org.da_scegliere.progetto_ids_hackathon.core.enums.state.hackathon.HackathonState;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
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
    private final Clock clock;

    /**
     * Assigns multiple staff members to a hackathon.
     *
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
    public Hackathon addStaffMembers(UUID hackathonId, Map<UUID, StaffRole> staffMembersIdMap) {
        log.info("Adding staff members to hackathon {}", hackathonId);
        if (staffMembersIdMap == null) {
            throw new IllegalArgumentException("staffMembersIdMap must not be null.");
        }
        if (staffMembersIdMap.isEmpty()) {
            throw new IllegalArgumentException("staffMembersIdMap must not be empty.");
        }

        Hackathon hackathon = hackathonCrudService.getHackathonById(hackathonId);
        validateStaffManagementState(hackathon, LocalDate.now(clock));

        for (Map.Entry<UUID, StaffRole> entry : staffMembersIdMap.entrySet()) {
            UUID staffId = entry.getKey();
            StaffRole role = entry.getValue();
            if (staffId == null || role == null) {
                throw new IllegalArgumentException("staffMembersIdMap must contain only non-null keys and values.");
            }

            StaffMember member = staffMemberRepository.findById(staffId)
                    .orElseThrow(() -> new IllegalArgumentException("Staff member not found: " + staffId + "."));
            StaffAssignment assignment = new StaffAssignment(LocalDate.now(clock), role, member, null);
            hackathon.addStaffAssignment(assignment);
        }

        log.info("Added staff members to hackathon {}", hackathonId);
        return hackathon;
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

        Set<UUID> idsToDelete = staffMembersId.stream().collect(Collectors.toSet());
        int removedCount = hackathon.removeStaffAssignmentsByStaffMemberIds(idsToDelete);
        if (removedCount != idsToDelete.size()) {
            throw new IllegalArgumentException("One or more staff members are not assigned to this hackathon.");
        }

        log.info("Deleted staff members from hackathon {}", hackathonId);
        return hackathon;
    }

    private static void validateStaffManagementState(Hackathon hackathon, LocalDate referenceDate) {
        HackathonState hackathonState = hackathon.getHackathonStateAt(referenceDate);
        if (hackathonState == HackathonState.EVALUATION || hackathonState == HackathonState.ENDED) {
            throw new InvalidHackathonStateOperationException(hackathonState, "Staff management");
        }
    }
}

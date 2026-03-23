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

package org.da_scegliere.progetto_ids_hackathon.application.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.da_scegliere.progetto_ids_hackathon.application.ports.events.DomainEventPublisher;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.*;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.staff.StaffEmailAlreadyInUseException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.staff.StaffMemberNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.Hackathon;
import org.da_scegliere.progetto_ids_hackathon.core.entities.staff.StaffAssignment;
import org.da_scegliere.progetto_ids_hackathon.core.entities.staff.StaffMember;
import org.da_scegliere.progetto_ids_hackathon.core.events.staff.StaffMemberCreatedEvent;
import org.da_scegliere.progetto_ids_hackathon.core.events.staff.StaffMemberNameChangedEvent;
import org.da_scegliere.progetto_ids_hackathon.core.enums.StaffRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Application service dedicated to staff member account management.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StaffService {

    private final IStaffMemberRepository staffMemberRepository;
    private final IStaffAssignmentRepository staffAssignmentRepository;
    private final IHackathonRepository hackathonRepository;
    private final IUserRepository userRepository;
    private final IManagerRepository managerRepository;
    private final DomainEventPublisher domainEventPublisher;

    /**
     * Retrieves all staff members.
     *
     * @return immutable snapshot of all staff members.
     */
    public List<StaffMember> getAllStaffMembers() {
        log.debug("Retrieving all staff members.");
        List<StaffMember> staffMembers = List.copyOf(staffMemberRepository.findAll());
        log.debug("Retrieved {} staff members.", staffMembers.size());
        return staffMembers;
    }

    /**
     * Retrieves all staff assignments.
     *
     * @return immutable snapshot of all assignments.
     */
    public List<StaffAssignment> getAllStaffAssignments() {
        log.debug("Retrieving all staff assignments.");
        List<StaffAssignment> assignments = List.copyOf(staffAssignmentRepository.findAll());
        log.debug("Retrieved {} staff assignments.", assignments.size());
        return assignments;
    }

    /**
     * Retrieves one staff member by identifier.
     *
     * @param staffMemberId staff identifier.
     * @return persisted staff member.
     */
    public StaffMember getStaffMemberById(UUID staffMemberId) {
        if (staffMemberId == null) {
            throw new IllegalArgumentException("staffMemberId must not be null.");
        }
        log.debug("Retrieving staff member by id={}.", staffMemberId);
        StaffMember staffMember = staffMemberRepository.findById(staffMemberId)
                .orElseThrow(() -> new StaffMemberNotFoundException(staffMemberId));
        log.debug("Retrieved staff member id={}.", staffMemberId);
        return staffMember;
    }

    /**
     * Retrieves one staff member by email.
     *
     * @param email staff email.
     * @return persisted staff member.
     */
    public StaffMember getStaffMemberByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email must not be blank.");
        }
        log.debug("Retrieving staff member by email={}.", email);
        StaffMember staffMember = staffMemberRepository.findByEmail(email)
                .orElseThrow(() -> new StaffMemberNotFoundException(email));
        log.debug("Retrieved staff member by email={}.", email);
        return staffMember;
    }

    /**
     * Retrieves staff assignments for one hackathon.
     *
     * @param hackathonId hackathon identifier.
     * @return immutable list of assignments.
     */
    public List<StaffAssignment> getStaffAssignmentsByHackathon(UUID hackathonId) {
        if (hackathonId == null) {
            throw new IllegalArgumentException("hackathonId must not be null.");
        }
        log.debug("Retrieving staff assignments for hackathonId={}.", hackathonId);
        List<StaffAssignment> assignments = List.copyOf(staffAssignmentRepository.findByHackathon_Id(hackathonId));
        log.debug("Retrieved {} staff assignments for hackathonId={}.", assignments.size(), hackathonId);
        return assignments;
    }

    /**
     * Retrieves staff assignments by role across all hackathons.
     *
     * @param staffRole target role.
     * @return immutable list of assignments.
     */
    public List<StaffAssignment> getStaffAssignmentsByRole(StaffRole staffRole) {
        if (staffRole == null) {
            throw new IllegalArgumentException("staffRole must not be null.");
        }
        log.debug("Retrieving staff assignments for role={}.", staffRole);
        List<StaffAssignment> assignments = List.copyOf(staffAssignmentRepository.findByStaffRole(staffRole));
        log.debug("Retrieved {} staff assignments for role={}.", assignments.size(), staffRole);
        return assignments;
    }

    /**
     * Retrieves staff assignments by role within one hackathon.
     *
     * @param hackathonId hackathon identifier.
     * @param staffRole   target role.
     * @return immutable list of assignments.
     */
    public List<StaffAssignment> getStaffAssignmentsByHackathonAndRole(UUID hackathonId, StaffRole staffRole) {
        if (hackathonId == null) {
            throw new IllegalArgumentException("hackathonId must not be null.");
        }
        if (staffRole == null) {
            throw new IllegalArgumentException("staffRole must not be null.");
        }
        log.debug("Retrieving staff assignments for hackathonId={} role={}.", hackathonId, staffRole);
        List<StaffAssignment> assignments = List.copyOf(
                staffAssignmentRepository.findByHackathon_IdAndStaffRole(hackathonId, staffRole)
        );
        log.debug(
                "Retrieved {} staff assignments for hackathonId={} role={}.",
                assignments.size(),
                hackathonId,
                staffRole
        );
        return assignments;
    }

    /**
     * Retrieves staff assignments for one staff member.
     *
     * @param staffMemberId staff member identifier.
     * @return immutable list of assignments.
     */
    public List<StaffAssignment> getStaffAssignmentsByStaffMember(UUID staffMemberId) {
        if (staffMemberId == null) {
            throw new IllegalArgumentException("staffMemberId must not be null.");
        }
        getStaffMemberById(staffMemberId);
        log.debug("Retrieving staff assignments for staffMemberId={}.", staffMemberId);
        List<StaffAssignment> assignments = List.copyOf(staffAssignmentRepository.findByStaffMember_Id(staffMemberId));
        log.debug("Retrieved {} staff assignments for staffMemberId={}.", assignments.size(), staffMemberId);
        return assignments;
    }

    /**
     * Retrieves staff assignments for one staff member filtered by role.
     *
     * @param staffMemberId staff member identifier.
     * @param staffRole target role.
     * @return immutable list of assignments.
     */
    public List<StaffAssignment> getStaffAssignmentsByStaffMemberAndRole(UUID staffMemberId, StaffRole staffRole) {
        if (staffRole == null) {
            throw new IllegalArgumentException("staffRole must not be null.");
        }
        if (staffMemberId == null) {
            throw new IllegalArgumentException("staffMemberId must not be null.");
        }
        getStaffMemberById(staffMemberId);
        log.debug("Retrieving staff assignments for staffMemberId={} role={}.", staffMemberId, staffRole);
        List<StaffAssignment> assignments = List.copyOf(
                staffAssignmentRepository.findByStaffMember_IdAndStaffRole(staffMemberId, staffRole)
        );
        log.debug(
                "Retrieved {} staff assignments for staffMemberId={} role={}.",
                assignments.size(),
                staffMemberId,
                staffRole
        );
        return assignments;
    }

    /**
     * Retrieves staff assignments for one staff member within one hackathon.
     *
     * @param staffMemberId staff member identifier.
     * @param hackathonId hackathon identifier.
     * @return immutable list of assignments.
     */
    public List<StaffAssignment> getStaffAssignmentsByStaffMemberAndHackathon(UUID staffMemberId, UUID hackathonId) {
        if (staffMemberId == null) {
            throw new IllegalArgumentException("staffMemberId must not be null.");
        }
        if (hackathonId == null) {
            throw new IllegalArgumentException("hackathonId must not be null.");
        }
        List<StaffAssignment> assignments = getStaffAssignmentsByStaffMember(staffMemberId).stream()
                .filter(assignment -> assignment.getHackathon() != null)
                .filter(assignment -> hackathonId.equals(assignment.getHackathon().getId()))
                .toList();
        log.debug(
                "Retrieved {} staff assignments for staffMemberId={} hackathonId={}.",
                assignments.size(),
                staffMemberId,
                hackathonId
        );
        return assignments;
    }

    /**
     * Retrieves unique staff members currently assigned to a hackathon.
     *
     * @param hackathonId hackathon identifier.
     * @return immutable list of unique staff members.
     */
    public List<StaffMember> getStaffMembersByHackathon(UUID hackathonId) {
        List<StaffMember> staffMembers = mapDistinctMembers(getStaffAssignmentsByHackathon(hackathonId));
        log.debug("Retrieved {} distinct staff members for hackathonId={}.", staffMembers.size(), hackathonId);
        return staffMembers;
    }

    /**
     * Retrieves unique staff members by assignment role.
     *
     * @param staffRole target role.
     * @return immutable list of unique staff members.
     */
    public List<StaffMember> getStaffMembersByRole(StaffRole staffRole) {
        List<StaffMember> staffMembers = mapDistinctMembers(getStaffAssignmentsByRole(staffRole));
        log.debug("Retrieved {} distinct staff members for role={}.", staffMembers.size(), staffRole);
        return staffMembers;
    }

    /**
     * Retrieves unique staff members by role within one hackathon.
     *
     * @param hackathonId hackathon identifier.
     * @param staffRole   target role.
     * @return immutable list of unique staff members.
     */
    public List<StaffMember> getStaffMembersByHackathonAndRole(UUID hackathonId, StaffRole staffRole) {
        List<StaffMember> staffMembers = mapDistinctMembers(getStaffAssignmentsByHackathonAndRole(hackathonId, staffRole));
        log.debug(
                "Retrieved {} distinct staff members for hackathonId={} role={}.",
                staffMembers.size(),
                hackathonId,
                staffRole
        );
        return staffMembers;
    }

    /**
     * Retrieves unique hackathons managed by one staff member.
     *
     * @param staffMemberId staff identifier.
     * @return immutable list of managed hackathons.
     */
    public List<Hackathon> getHackathonsManagedByStaffMember(UUID staffMemberId) {
        getStaffMemberById(staffMemberId);
        List<Hackathon> hackathons = mapDistinctHackathons(staffAssignmentRepository.findByStaffMember_Id(staffMemberId));
        log.debug("Retrieved {} managed hackathons for staffMemberId={}.", hackathons.size(), staffMemberId);
        return hackathons;
    }

    /**
     * Retrieves unique hackathons managed by one staff member with a specific role.
     *
     * @param staffMemberId staff identifier.
     * @param staffRole     role used in assignments.
     * @return immutable list of managed hackathons.
     */
    public List<Hackathon> getHackathonsManagedByStaffMemberAndRole(UUID staffMemberId, StaffRole staffRole) {
        if (staffRole == null) {
            throw new IllegalArgumentException("staffRole must not be null.");
        }
        getStaffMemberById(staffMemberId);
        List<Hackathon> hackathons = mapDistinctHackathons(
                staffAssignmentRepository.findByStaffMember_IdAndStaffRole(staffMemberId, staffRole)
        );
        log.debug(
                "Retrieved {} managed hackathons for staffMemberId={} role={}.",
                hackathons.size(),
                staffMemberId,
                staffRole
        );
        return hackathons;
    }

    /**
     * Retrieves managed hackathons for one staff member grouped by assignment role.
     *
     * @param staffMemberId staff identifier.
     * @return immutable map role -> immutable list of hackathons.
     */
    public Map<StaffRole, List<Hackathon>> getHackathonsManagedByStaffMemberPerRole(UUID staffMemberId) {
        getStaffMemberById(staffMemberId);
        Map<StaffRole, List<Hackathon>> result =
                groupHackathonsPerRole(staffAssignmentRepository.findByStaffMember_Id(staffMemberId));
        log.debug("Retrieved managed hackathons grouped by role for staffMemberId={}.", staffMemberId);
        return result;
    }

    /**
     * Retrieves managed hackathons grouped by assignment role across all staff members.
     *
     * @return immutable map role -> immutable list of hackathons.
     */
    public Map<StaffRole, List<Hackathon>> getHackathonsManagedPerRole() {
        log.debug("Retrieving managed hackathons grouped by role for all staff members.");
        Map<StaffRole, List<Hackathon>> result = groupHackathonsPerRole(staffAssignmentRepository.findAll());
        log.debug("Retrieved managed hackathons grouped by role for all staff members.");
        return result;
    }

    /**
     * Creates and persists a new staff member.
     *
     * @param name  staff display name.
     * @param age   staff age.
     * @param email staff email.
     * @return persisted staff member.
     */
    @Transactional
    public StaffMember createStaffMember(String name, int age, String email) {
        log.info("Creating staff member with email={}.", email);
        ensureEmailIsAvailable(email);

        StaffMember staffMember = new StaffMember(name, age, email, new ArrayList<>());
        StaffMember savedStaffMember = staffMemberRepository.save(staffMember);

        domainEventPublisher.publish(new StaffMemberCreatedEvent(savedStaffMember));

        log.info("Created staff member id={}.", savedStaffMember.getId());
        return savedStaffMember;
    }

    /**
     * Updates the staff display name.
     *
     * @param staffMemberId staff identifier.
     * @param newName       replacement display name.
     * @return persisted updated staff member.
     */
    @Transactional
    public StaffMember changeStaffMemberName(UUID staffMemberId, String newName) {
        log.info("Changing staff member name for staffMemberId={}.", staffMemberId);

        StaffMember staffMember = getStaffMemberById(staffMemberId);
        staffMember.setName(newName);
        StaffMember updatedStaffMember = staffMemberRepository.save(staffMember);

        domainEventPublisher.publish(new StaffMemberNameChangedEvent(updatedStaffMember, newName));

        log.info("Changed staff member name for staffMemberId={}.", staffMemberId);
        return updatedStaffMember;
    }

    /**
     * Deletes one staff member by identifier.
     *
     * @param staffMemberId staff identifier.
     */
    @Transactional
    public void deleteStaffMember(UUID staffMemberId) {
        log.info("Deleting staff member staffMemberId={}.", staffMemberId);
        staffMemberRepository.delete(getStaffMemberById(staffMemberId));
        log.info("Deleted staff member staffMemberId={}.", staffMemberId);
    }

    private void ensureEmailIsAvailable(String normalizedEmail) {
        boolean alreadyUsed = staffMemberRepository.existsByEmailIgnoreCase(normalizedEmail)
                || userRepository.existsByEmailIgnoreCase(normalizedEmail)
                || managerRepository.existsByEmailIgnoreCase(normalizedEmail);
        if (alreadyUsed) {
            throw new StaffEmailAlreadyInUseException(normalizedEmail);
        }
    }

    private List<StaffMember> mapDistinctMembers(List<StaffAssignment> assignments) {
        Map<UUID, StaffMember> uniqueMembers = new LinkedHashMap<>();
        for (StaffAssignment assignment : assignments) {
            if (assignment == null || assignment.getStaffMember() == null || assignment.getStaffMember().getId() == null) {
                continue;
            }
            uniqueMembers.putIfAbsent(assignment.getStaffMember().getId(), assignment.getStaffMember());
        }
        return List.copyOf(uniqueMembers.values());
    }

    private List<Hackathon> mapDistinctHackathons(List<StaffAssignment> assignments) {
        Map<UUID, Hackathon> uniqueHackathons = new LinkedHashMap<>();
        for (StaffAssignment assignment : assignments) {
            if (assignment == null || assignment.getHackathon() == null || assignment.getHackathon().getId() == null) {
                continue;
            }
            Hackathon hackathon = assignment.getHackathon();
            uniqueHackathons.putIfAbsent(hackathon.getId(), hackathon);
        }
        return List.copyOf(uniqueHackathons.values());
    }

    private Map<StaffRole, List<Hackathon>> groupHackathonsPerRole(List<StaffAssignment> assignments) {
        Map<StaffRole, Map<UUID, Hackathon>> groupedHackathons = new EnumMap<>(StaffRole.class);
        for (StaffRole role : StaffRole.values()) {
            groupedHackathons.put(role, new LinkedHashMap<>());
        }

        for (StaffAssignment assignment : assignments) {
            if (assignment == null || assignment.getStaffRole() == null) {
                continue;
            }
            Hackathon hackathon = assignment.getHackathon();
            if (hackathon == null || hackathon.getId() == null) {
                continue;
            }
            groupedHackathons.get(assignment.getStaffRole()).putIfAbsent(hackathon.getId(), hackathon);
        }

        Map<StaffRole, List<Hackathon>> result = new EnumMap<>(StaffRole.class);
        for (StaffRole role : StaffRole.values()) {
            result.put(role, List.copyOf(groupedHackathons.get(role).values()));
        }
        return Map.copyOf(result);
    }
}

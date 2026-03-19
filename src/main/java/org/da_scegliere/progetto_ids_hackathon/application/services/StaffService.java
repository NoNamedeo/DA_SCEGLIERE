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
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IStaffAssignmentRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IManagerRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IStaffMemberRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IUserRepository;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.moderation.StaffEmailAlreadyInUseException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.staff.StaffMemberNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.core.entities.staff.StaffAssignment;
import org.da_scegliere.progetto_ids_hackathon.core.entities.staff.StaffMember;
import org.da_scegliere.progetto_ids_hackathon.core.enums.StaffRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Application service dedicated to staff member account management.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StaffService {

    private final IStaffMemberRepository staffMemberRepository;
    private final IStaffAssignmentRepository staffAssignmentRepository;
    private final IUserRepository userRepository;
    private final IManagerRepository managerRepository;

    /**
     * Retrieves all staff members.
     *
     * @return immutable snapshot of all staff members.
     */
    public List<StaffMember> getAllStaffMembers() {
        return List.copyOf(staffMemberRepository.findAll());
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
        return staffMemberRepository.findById(staffMemberId)
                .orElseThrow(() -> new StaffMemberNotFoundException(staffMemberId));
    }

    /**
     * Retrieves one staff member by email.
     *
     * @param email staff email.
     * @return persisted staff member.
     */
    public StaffMember getStaffMemberByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        return staffMemberRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new StaffMemberNotFoundException(normalizedEmail));
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
        return List.copyOf(staffAssignmentRepository.findByHackathon_Id(hackathonId));
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
        return List.copyOf(staffAssignmentRepository.findByStaffRole(staffRole));
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
        return List.copyOf(staffAssignmentRepository.findByHackathon_IdAndStaffRole(hackathonId, staffRole));
    }

    /**
     * Retrieves unique staff members currently assigned to a hackathon.
     *
     * @param hackathonId hackathon identifier.
     * @return immutable list of unique staff members.
     */
    public List<StaffMember> getStaffMembersByHackathon(UUID hackathonId) {
        return mapDistinctMembers(getStaffAssignmentsByHackathon(hackathonId));
    }

    /**
     * Retrieves unique staff members by assignment role.
     *
     * @param staffRole target role.
     * @return immutable list of unique staff members.
     */
    public List<StaffMember> getStaffMembersByRole(StaffRole staffRole) {
        return mapDistinctMembers(getStaffAssignmentsByRole(staffRole));
    }

    /**
     * Retrieves unique staff members by role within one hackathon.
     *
     * @param hackathonId hackathon identifier.
     * @param staffRole   target role.
     * @return immutable list of unique staff members.
     */
    public List<StaffMember> getStaffMembersByHackathonAndRole(UUID hackathonId, StaffRole staffRole) {
        return mapDistinctMembers(getStaffAssignmentsByHackathonAndRole(hackathonId, staffRole));
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
        String normalizedName = normalizeName(name);
        String normalizedEmail = normalizeEmail(email);
        ensureEmailIsAvailable(normalizedEmail);

        StaffMember staffMember = new StaffMember(normalizedName, age, normalizedEmail, new ArrayList<>());
        return staffMemberRepository.save(staffMember);
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
        StaffMember staffMember = getStaffMemberById(staffMemberId);
        staffMember.setName(normalizeName(newName));
        return staffMemberRepository.save(staffMember);
    }

    /**
     * Deletes one staff member by identifier.
     *
     * @param staffMemberId staff identifier.
     */
    @Transactional
    public void deleteStaffMember(UUID staffMemberId) {
        staffMemberRepository.delete(getStaffMemberById(staffMemberId));
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

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email must not be blank.");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank.");
        }
        return name.trim();
    }
}

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
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IHackathonRepository;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.hackathon.HackathonNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.Participation;
import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.Hackathon;
import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.builder.IHackathonBuilder;
import org.da_scegliere.progetto_ids_hackathon.core.entities.staff.StaffAssignment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Application service exposing CRUD-oriented operations for hackathon aggregates.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Lookup hackathons by id/name.</li>
 *     <li>Create and update core mutable hackathon data.</li>
 *     <li>Delete hackathon records.</li>
 * </ul>
 * Domain invariants beyond plain CRUD are intentionally delegated to domain models
 * or dedicated use-case services.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class HackathonCrudService {

    private final IHackathonRepository hackathonRepository;

    private final IHackathonBuilder hackathonBuilder;

    /**
     * Retrieves all hackathons.
     *
     * @return immutable snapshot of all hackathons.
     */
    public List<Hackathon> getAllHackathons() {
        return List.copyOf(hackathonRepository.findAll());
    }

    /**
     * Retrieves a hackathon by identifier.
     *
     * @param hackathonId hackathon identifier.
     * @return resolved hackathon.
     * @throws IllegalArgumentException when {@code hackathonId} is {@code null}.
     * @throws HackathonNotFoundException when hackathon does not exist.
     */
    public Hackathon getHackathonById(UUID hackathonId) {
        if (hackathonId == null) {
            throw new IllegalArgumentException("hackathonId must not be null.");
        }
        return hackathonRepository.findById(hackathonId)
                .orElseThrow(() -> new HackathonNotFoundException(hackathonId));
    }

    /**
     * Retrieves a hackathon by name.
     *
     * @param name hackathon name.
     * @return resolved hackathon.
     * @throws IllegalArgumentException when {@code name} is blank.
     * @throws HackathonNotFoundException when no hackathon exists with the given name.
     */
    public Hackathon getHackathonByName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank.");
        }
        return hackathonRepository.findHackathonByName(name)
                .orElseThrow(() -> new HackathonNotFoundException(name));
    }

    /**
     * Creates and persists a new hackathon aggregate.
     *
     * @param name hackathon name.
     * @param description hackathon description/regulation summary.
     * @param participations initial participation's list.
     * @param staffAssignments initial staff assignments list.
     * @return persisted hackathon.
     * @throws IllegalArgumentException when mandatory input is invalid.
     */
    @Transactional
    public Hackathon createHackathon(String name, String description, List<Participation> participations, List<StaffAssignment> staffAssignments) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank.");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("description must not be blank.");
        }
        if (participations == null) {
            throw new IllegalArgumentException("participations must not be null.");
        }
        if (staffAssignments == null) {
            throw new IllegalArgumentException("staffAssignments must not be null.");
        }

        return hackathonRepository.save(hackathonBuilder.reset()
                .setName(name)
                .setDescription(description)
                .setParticipations(participations)
                .setStaff(staffAssignments)
                .build());
    }

    /**
     * Updates hackathon description.
     *
     * @param hackathonId hackathon identifier.
     * @param description new description.
     * @return updated hackathon aggregate.
     * @throws IllegalArgumentException when description is blank.
     * @throws HackathonNotFoundException when hackathon does not exist.
     */
    @Transactional
    public Hackathon changeDescription(UUID hackathonId, String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("description must not be blank.");
        }
        Hackathon hackathon = getHackathonById(hackathonId);
        hackathon.setDescription(description);
        return hackathon;
    }

    /**
     * Replaces hackathon participations collection.
     *
     * @param hackathonId hackathon identifier.
     * @param participations new participations collection.
     * @return updated hackathon aggregate.
     * @throws IllegalArgumentException when participations is {@code null}.
     * @throws HackathonNotFoundException when hackathon does not exist.
     */
    @Transactional
    public Hackathon changeParticipations(UUID hackathonId, List<Participation> participations) {
        if (participations == null) {
            throw new IllegalArgumentException("participations must not be null.");
        }
        Hackathon hackathon = getHackathonById(hackathonId);
        hackathon.setParticipations(participations);
        return hackathon;
    }

    /**
     * Replaces hackathon staff assignments collection.
     *
     * @param hackathonId hackathon identifier.
     * @param staffAssignments new staff assignments collection.
     * @return updated hackathon aggregate.
     * @throws IllegalArgumentException when staff assignments is {@code null}.
     * @throws HackathonNotFoundException when hackathon does not exist.
     */
    @Transactional
    public Hackathon changeStaff(UUID hackathonId, List<StaffAssignment> staffAssignments) {
        if (staffAssignments == null) {
            throw new IllegalArgumentException("staffAssignments must not be null.");
        }
        Hackathon hackathon = getHackathonById(hackathonId);
        hackathon.setStaff(staffAssignments);
        return hackathon;
    }

    /**
     * Deletes a hackathon by identifier.
     *
     * @param hackathonId hackathon identifier.
     * @throws HackathonNotFoundException when hackathon does not exist.
     */
    @Transactional
    public void deleteHackathon(UUID hackathonId) {
        hackathonRepository.delete(getHackathonById(hackathonId));
    }

    /**
     * Checks whether a hackathon exists by identifier.
     *
     * @param hackathonId hackathon identifier.
     * @return {@code true} if an entity exists for the given id, {@code false} otherwise.
     * @throws IllegalArgumentException when {@code hackathonId} is {@code null}.
     */
    public boolean existsById(UUID hackathonId) {
        if (hackathonId == null) {
            throw new IllegalArgumentException("hackathonId must not be null.");
        }
        return hackathonRepository.existsById(hackathonId);
    }
}

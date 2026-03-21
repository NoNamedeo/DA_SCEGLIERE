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
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IHackathonRepository;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.hackathon.HackathonNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.Participation;
import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.Hackathon;
import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.builder.HackathonBuilder;
import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.builder.HackathonBuilderDirector;
import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.builder.IHackathonBuilder;
import org.da_scegliere.progetto_ids_hackathon.core.entities.staff.StaffAssignment;
import org.da_scegliere.progetto_ids_hackathon.core.enums.state.hackathon.HackathonState;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Application service exposing CRUD-oriented operations for hackathon aggregates.
 * <p>
 * Domain invariants beyond plain CRUD are intentionally delegated to domain models
 * or dedicated use-case services.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class HackathonCrudService {

    private final IHackathonRepository hackathonRepository;

    /**
     * Retrieves all hackathons.
     *
     * @return immutable snapshot of all hackathons.
     */
    public List<Hackathon> getAllHackathons() {
        log.info("Getting all hackathons");
        return List.copyOf(hackathonRepository.findAll());
    }

    /**
     * Retrieves all hackathons by the provided state.
     *
     * @return immutable snapshot of all hackathons.
     */
    public List<Hackathon> getAllHackathonsByState( HackathonState hackathonState ) {
        log.info("Getting all hackathons by hackathonState: {}", hackathonState);
        return List.copyOf(hackathonRepository.findAll()
                .stream()
                .filter(hackathon ->
                        hackathon.getHackathonState().equals(hackathonState))
                .toList());
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
        log.info("Getting hackathon hackathonId={}", hackathonId);

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
    public List<Hackathon> getHackathonByName(String name) {
        log.info("Getting hackathon name={}", name);

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank.");
        }

        List<Hackathon> result = hackathonRepository.findHackathonByName(name);

        if (result.isEmpty()) {
            throw new HackathonNotFoundException(name);
        }

        return result;
    }

    /**
     * Retrieves hackathons by the provided state and name.
     *
     * @return immutable snapshot of all hackathons.
     */
    public List<Hackathon> getAllHackathonsByNameAndState( String name,  HackathonState hackathonState ) {
        log.info("Getting all hackathons by name {} and hackathonState: {}", name, hackathonState);
        return List.copyOf(getHackathonByName(name)
                .stream()
                .filter(hackathon ->
                        hackathon.getHackathonState().equals(hackathonState)).toList());
    }

    /**
     * Creates and persists a new hackathon aggregate.
     *
     * @param name hackathon name.
     * @param description hackathon description/regulation summary.
     * @param participations initial participation's list.
     * @param staffAssignments initial staff assignments list.
     * @param awardPrize award prize of the hackathon.
     * @param builderConfigurer can be used to apply default build config
     *                          (ex. HackathonBuilderDirector::makeShortHackathonDeadlines)
     * @return persisted hackathon.
     * @throws IllegalArgumentException when mandatory input is invalid.
     */
    @Transactional
    public Hackathon createHackathon(
            String name,
            String description,
            List<Participation> participations,
            List<StaffAssignment> staffAssignments,
            BigDecimal awardPrize,
            Consumer<IHackathonBuilder> builderConfigurer
    ) {
        log.info("Creating hackathon {}", name);

        IHackathonBuilder builder = new HackathonBuilder();

        if (builderConfigurer != null) {
            builderConfigurer.accept(builder);
        }

        Hackathon hackathon = builder
                .setName(name)
                .setDescription(description)
                .setParticipations(participations)
                .setStaff(staffAssignments)
                .setAwardPrize(awardPrize)
                .build();

        log.info("Hackathon created hackathonId={}", hackathon.getId());
        return hackathonRepository.save(hackathon);
    }

    /**
     * Creates and persists a new long term hackathon aggregate.
     *
     * @param hackathonId hackathon id.
     * @param registrationDeadline change hackathon registration deadLine if not null.
     * @param submissionDeadline change hackathon submission deadLine if not null.
     * @param evaluationDeadline change hackathon evaluation deadLine if not null.
     * @return persisted hackathon.
     * @throws IllegalArgumentException when hackathonId is null.
     */
    @Transactional
    public Hackathon changeHackathonDeadlines(
            UUID hackathonId,
            LocalDate registrationDeadline,
            LocalDate submissionDeadline,
            LocalDate evaluationDeadline
    ) {
        if (hackathonId == null) {
            throw new IllegalArgumentException("hackathonId must not be null.");
        }

        Hackathon hackathon = getHackathonById(hackathonId);

        hackathon.configureTimeline(
                registrationDeadline != null ? registrationDeadline : hackathon.getRegistrationDeadline(),
                submissionDeadline != null ? submissionDeadline : hackathon.getSubmissionDeadline(),
                evaluationDeadline != null ? evaluationDeadline : hackathon.getEvaluationDeadline()
        );

        return hackathon;
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
        log.info("Changing hackathon description={} hackathonId={}", description, hackathonId);

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("description must not be blank.");
        }
        Hackathon hackathon = getHackathonById(hackathonId);
        hackathon.setDescription(description);

        log.info("Changed hackathon description={} hackathonId={}", hackathon.getDescription(), hackathonId);
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
        log.info("Changing participations hackathonId={}", hackathonId);

        if (participations == null) {
            throw new IllegalArgumentException("participations must not be null.");
        }
        Hackathon hackathon = getHackathonById(hackathonId);
        hackathon.setParticipations(participations);

        log.info("Changed participations hackathonId={}", hackathonId);
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
        log.info("Changing hackathon hackathonId={} staff assignments", hackathonId);

        if (staffAssignments == null) {
            throw new IllegalArgumentException("staffAssignments must not be null.");
        }
        Hackathon hackathon = getHackathonById(hackathonId);
        hackathon.setStaff(staffAssignments);

        log.info("Changed hackathon hackathonId={} staff assignments", hackathonId);
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
        log.info("Deleting hackathon {}", hackathonId);

        hackathonRepository.delete(getHackathonById(hackathonId));

        log.info("Deleted hackathon {}", hackathonId);
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

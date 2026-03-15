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

import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.hackathon.InvalidHackathonStateOperationException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.hackathon.InvalidHackathonStateTransitionException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.hackathon.WinnerAssignmentNotAllowedException;
import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.Hackathon;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Team;
import org.da_scegliere.progetto_ids_hackathon.core.enums.states.hackathon.HackathonState;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

/**
 * Application service for hackathon lifecycle use cases.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Orchestrate state transitions of a hackathon aggregate.</li>
 *     <li>Delegate winner assignment to domain logic and map domain failures into application exceptions.</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class HackathonLifecycleService {

    private final HackathonCrudService hackathonCrudService;

    /**
     * Creates a new service instance.
     *
     * @param hackathonCrudService dependency used to resolve hackathon aggregates.
     * @throws NullPointerException when {@code hackathonCrudService} is {@code null}.
     */
    public HackathonLifecycleService(HackathonCrudService hackathonCrudService) {
        this.hackathonCrudService = Objects.requireNonNull(hackathonCrudService, "hackathonCrudService must not be null.");
    }

    /**
     * Transitions a hackathon to an explicit target state.
     *
     * @param hackathonId hackathon identifier.
     * @param targetState target lifecycle state.
     * @return hackathon after transition.
     * @throws IllegalArgumentException when {@code targetState} is {@code null}.
     * @throws org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.hackathon.HackathonNotFoundException
     *         when hackathon does not exist.
     * @throws InvalidHackathonStateTransitionException when transition is not valid according to domain rules.
     */
    @Transactional
    public Hackathon transitionHackathonState(UUID hackathonId, HackathonState targetState) {
        if (targetState == null) {
            throw new IllegalArgumentException("targetState must not be null.");
        }

        Hackathon hackathon = hackathonCrudService.getHackathonById(hackathonId);
        try {
            hackathon.transitionTo(targetState);
        } catch (IllegalStateException ex) {
            throw new InvalidHackathonStateTransitionException(hackathon.getHackathonState(), targetState, ex);
        }
        return hackathon;
    }

    /**
     * Advances a hackathon to its next lifecycle state.
     *
     * @param hackathonId hackathon identifier.
     * @return hackathon after state advancement.
     * @throws org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.hackathon.HackathonNotFoundException
     *         when hackathon does not exist.
     * @throws InvalidHackathonStateTransitionException when advancement is not allowed.
     */
    @Transactional
    public Hackathon advanceHackathonState(UUID hackathonId) {
        Hackathon hackathon = hackathonCrudService.getHackathonById(hackathonId);
        try {
            hackathon.advanceState();
        } catch (IllegalStateException ex) {
            throw new InvalidHackathonStateTransitionException(hackathon.getHackathonState(), null, ex);
        }
        return hackathon;
    }

    /**
     * Assigns the winner team to the hackathon.
     * <p>
     * Business validation is delegated to the domain aggregate
     * ({@link Hackathon#assignWinner(Team)}), while this method maps domain failures
     * into application-level exceptions.
     *
     * @param hackathonId hackathon identifier.
     * @param winnerTeam winner team candidate.
     * @return hackathon with assigned winner.
     * @throws IllegalArgumentException when winner team input is invalid.
     * @throws org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.hackathon.HackathonNotFoundException
     *         when hackathon does not exist.
     * @throws InvalidHackathonStateOperationException when operation is invoked outside allowed state.
     * @throws WinnerAssignmentNotAllowedException when winner assignment violates business rules.
     */
    @Transactional
    public Hackathon assignWinner(UUID hackathonId, Team winnerTeam) {
        if (winnerTeam == null) {
            throw new IllegalArgumentException("winnerTeam must not be null.");
        }
        if (winnerTeam.getId() == null) {
            throw new IllegalArgumentException("winnerTeam.id must not be null.");
        }

        Hackathon hackathon = hackathonCrudService.getHackathonById(hackathonId);
        try {
            hackathon.assignWinner(winnerTeam);
        } catch (IllegalStateException ex) {
            if (hackathon.getHackathonState() != HackathonState.EVALUATION) {
                throw new InvalidHackathonStateOperationException(hackathon.getHackathonState(), "Assign winner");
            }
            throw new WinnerAssignmentNotAllowedException(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw new WinnerAssignmentNotAllowedException(ex.getMessage());
        }
        return hackathon;
    }
}

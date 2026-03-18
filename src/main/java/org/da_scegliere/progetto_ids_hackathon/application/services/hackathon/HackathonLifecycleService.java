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
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.ITeamRepository;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.hackathon.InvalidHackathonStateOperationException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.hackathon.WinnerAssignmentNotAllowedException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.team.TeamNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.Hackathon;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Team;
import org.da_scegliere.progetto_ids_hackathon.core.enums.state.hackathon.HackathonState;
import org.da_scegliere.progetto_ids_hackathon.core.policies.BusinessPolicy;
import org.da_scegliere.progetto_ids_hackathon.core.policies.hackathon.winner.WinnerAssignmentContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Application service for hackathon lifecycle use cases.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Resolve time-driven hackathon lifecycle state.</li>
 *     <li>Delegate winner assignment to domain logic and map domain failures into application exceptions.</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class HackathonLifecycleService {

    private final HackathonCrudService hackathonCrudService;
    private final ITeamRepository teamRepository;
    private final Clock clock;
    private final BusinessPolicy<WinnerAssignmentContext> winnerAssignmentPolicy;

    /**
     * Resolves the current time-driven lifecycle state of a hackathon.
     *
     * @param hackathonId hackathon identifier.
     * @return lifecycle state at current clock date.
     */
    public HackathonState determineCurrentState(UUID hackathonId) {
        Hackathon hackathon = hackathonCrudService.getHackathonById(hackathonId);
        return hackathon.getHackathonStateAt(LocalDate.now(clock));
    }

    /**
     * Concludes a hackathon by forcing ENDED state with time-driven timeline adjustment.
     *
     * @param hackathonId hackathon identifier.
     * @return updated hackathon aggregate.
     */
    @Transactional
    public Hackathon concludeHackathon(UUID hackathonId) {
        Hackathon hackathon = hackathonCrudService.getHackathonById(hackathonId);
        hackathon.concludeAt(LocalDate.now(clock));
        return hackathon;
    }

    /**
     * Assigns the winner team to the hackathon.
     * <p>
     * Business validation is delegated to the domain aggregate
     * ({@link Hackathon#assignWinner(Team, LocalDate, BusinessPolicy)}), while this method maps domain failures
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
        LocalDate today = LocalDate.now(clock);
        HackathonState currentState = hackathon.getHackathonStateAt(today);
        try {
            hackathon.assignWinner(winnerTeam, today, winnerAssignmentPolicy);
        } catch (IllegalStateException ex) {
            if (currentState != HackathonState.EVALUATION) {
                throw new InvalidHackathonStateOperationException(currentState, "Assign winner");
            }
            throw new WinnerAssignmentNotAllowedException(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw new WinnerAssignmentNotAllowedException(ex.getMessage());
        }
        return hackathon;
    }

    /**
     * Assigns winner team resolving the team by identifier.
     *
     * @param hackathonId hackathon identifier.
     * @param winnerTeamId winner team identifier.
     * @return hackathon with assigned winner.
     */
    @Transactional
    public Hackathon assignWinner(UUID hackathonId, UUID winnerTeamId) {
        if (winnerTeamId == null) {
            throw new IllegalArgumentException("winnerTeamId must not be null.");
        }
        Team winnerTeam = teamRepository.findById(winnerTeamId)
                .orElseThrow(() -> new TeamNotFoundException(winnerTeamId));
        return assignWinner(hackathonId, winnerTeam);
    }
}

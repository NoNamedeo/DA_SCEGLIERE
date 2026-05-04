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

package org.da_scegliere.progetto_ids_hackathon.core.policies.hackathon.winner;

import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Submission;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Team;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.TeamParticipation;
import org.da_scegliere.progetto_ids_hackathon.core.enums.state.hackathon.HackathonState;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable policy context for winner assignment validation.
 *
 * @param hackathonState current lifecycle state.
 * @param winnerTeam winner candidate.
 * @param teamParticipations hackathon team participations.
 * @param submissions all submissions belonging to the hackathon.
 */
public record WinnerAssignmentContext(
        HackathonState hackathonState,
        Team winnerTeam,
        List<TeamParticipation> teamParticipations,
        List<Submission> submissions
) {

    public WinnerAssignmentContext {
        Objects.requireNonNull(hackathonState, "hackathonState must not be null.");
        Objects.requireNonNull(winnerTeam, "winnerTeam must not be null.");
        Objects.requireNonNull(teamParticipations, "teamParticipations must not be null.");
        Objects.requireNonNull(submissions, "submissions must not be null.");

        teamParticipations = List.copyOf(teamParticipations);
        submissions = List.copyOf(submissions);
    }

    /**
     * Checks whether winner candidate is one of participating teams.
     *
     * @return {@code true} when winner team belongs to participation list.
     */
    public boolean winnerTeamParticipates() {
        UUID winnerId = winnerTeam.getId();
        if (winnerId == null) {
            return false;
        }
        return teamParticipations.stream()
                .filter(participation -> !participation.isDisqualified())
                .map(TeamParticipation::getTeam)
                .filter(Objects::nonNull)
                .map(Team::getId)
                .filter(Objects::nonNull)
                .anyMatch(winnerId::equals);
    }
}

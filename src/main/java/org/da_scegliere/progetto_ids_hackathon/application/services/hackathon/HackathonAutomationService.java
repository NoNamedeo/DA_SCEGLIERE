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
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IHackathonRepository;
import org.da_scegliere.progetto_ids_hackathon.application.services.PaymentService;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.hackathon.HackathonNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.Hackathon;
import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.Participation;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Team;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.TeamParticipation;
import org.da_scegliere.progetto_ids_hackathon.core.entities.user.User;
import org.da_scegliere.progetto_ids_hackathon.core.enums.state.hackathon.HackathonState;
import org.da_scegliere.progetto_ids_hackathon.core.events.hackathon.HackathonConcludedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Application service that orchestrates a single scheduler cycle for one hackathon.
 * <p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HackathonAutomationService {

    private final IHackathonRepository hackathonRepository;
    private final HackathonLifecycleService lifecycleService;
    private final PaymentService paymentService;
    private final DomainEventPublisher domainEventPublisher;

    /**
     * Processes one hackathon lifecycle step atomically.
     *
     * @param hackathonId hackathon identifier.
     */
    @Transactional
    public void processSingleHackathon(UUID hackathonId) {
        requireNonNullId(hackathonId);

        Hackathon hackathon = hackathonRepository.findByIdForUpdate(hackathonId)
                .orElseThrow(() -> new HackathonNotFoundException(hackathonId));
        HackathonState state = lifecycleService.determineCurrentState(hackathon);
        boolean winnerAssigned = false;
        boolean concluded = false;
        boolean prizeAwarded = false;

        if (hackathon.getWinner() == null && canAssignWinner(state)) {
            Team winner = lifecycleService.determineWinnerTeam(hackathon);
            lifecycleService.assignWinner(hackathon, winner);
            winnerAssigned = true;
            log.info("Winner assigned by automation hackathonId={} winnerTeamId={}.", hackathonId, winner.getId());
        }

        state = lifecycleService.determineCurrentState(hackathon);

        if (hackathon.getWinner() != null && state != HackathonState.ENDED) {
            lifecycleService.concludeHackathon(hackathon);
            List<User> participantsToNotify = collectConclusionNotificationRecipients(hackathon);
            if (!participantsToNotify.isEmpty()) {
                domainEventPublisher.publish(new HackathonConcludedEvent(
                        hackathon.getName(),
                        hackathon.getWinner().getName(),
                        participantsToNotify
                ));
            }
            int notifiedUsers = participantsToNotify.size();
            concluded = true;
            log.info("Hackathon concluded by automation hackathonId={} notifiedUsers={}.", hackathonId, notifiedUsers);
        }

        state = lifecycleService.determineCurrentState(hackathon);

        if (hackathon.getWinner() != null && hackathon.getPrizePaidAt() == null && state == HackathonState.ENDED) {
            prizeAwarded = paymentService.awardPrizeToWinner(hackathon.getAwardPrize(), hackathon);
            if (prizeAwarded) {
                log.info("Winner prize awarded by automation hackathonId={}.", hackathonId);
            }
        }

        log.debug(
                "Processed hackathon automation hackathonId={} winnerAssigned={} concluded={} prizeAwarded={}.",
                hackathonId,
                winnerAssigned,
                concluded,
                prizeAwarded
        );
    }

    private List<User> collectConclusionNotificationRecipients(Hackathon hackathon) {
        Team winner = hackathon.getWinner();
        if (winner == null) {
            return List.of();
        }

        List<Participation> participations = hackathon.getParticipations();
        if (participations == null || participations.isEmpty()) {
            return List.of();
        }

        Set<UUID> notifiedUserIds = new HashSet<>();
        List<User> recipients = new ArrayList<>();

        for (Participation participation : participations) {
            if (!(participation instanceof TeamParticipation teamParticipation)) {
                continue;
            }
            Team team = teamParticipation.getTeam();
            if (team == null || team.getMembers() == null) {
                continue;
            }

            for (User user : team.getMembers()) {
                if (user == null || user.getId() == null || !notifiedUserIds.add(user.getId())) {
                    continue;
                }
                recipients.add(user);
            }
        }
        return List.copyOf(recipients);
    }

    private static boolean canAssignWinner(HackathonState state) {
        return state == HackathonState.EVALUATION || state == HackathonState.ENDED;
    }

    private static void requireNonNullId(UUID hackathonId) {
        if (hackathonId == null) {
            throw new IllegalArgumentException("hackathonId must not be null.");
        }
    }

}

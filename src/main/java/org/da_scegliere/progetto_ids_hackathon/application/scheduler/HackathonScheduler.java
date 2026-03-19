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

package org.da_scegliere.progetto_ids_hackathon.application.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.da_scegliere.progetto_ids_hackathon.application.services.PaymentService;
import org.da_scegliere.progetto_ids_hackathon.application.services.hackathon.HackathonCrudService;
import org.da_scegliere.progetto_ids_hackathon.application.services.hackathon.HackathonLifecycleService;
import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.Hackathon;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Team;
import org.da_scegliere.progetto_ids_hackathon.core.enums.state.hackathon.HackathonState;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class HackathonScheduler {

    private final HackathonLifecycleService lifecycleService;
    private final PaymentService paymentService;
    private final HackathonCrudService hackathonCrudService;

    @Scheduled(cron = "0 */5 * * * *")
    public void processHackathonsLifecycle() {

        List<Hackathon> hackathons = hackathonCrudService.getAllHackathons();

        for (Hackathon hackathon : hackathons) {

            HackathonState state = lifecycleService.determineCurrentState(hackathon.getId());

            if (state == HackathonState.ENDED && hackathon.getWinner() == null) {

                try {
                    lifecycleService.concludeHackathon(hackathon.getId());

                    Team winner = resolveWinner(hackathon);
                    lifecycleService.assignWinner(hackathon.getId(), winner.getId());

                    // paga premio
                    paymentService.awardPrizeToWinner(
                            hackathon.getAwardPrize(),
                            hackathon.getId()
                    );

                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                }
            }
        }
    }
}

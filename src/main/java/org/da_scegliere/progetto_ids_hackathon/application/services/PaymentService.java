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
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IHackathonRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.INotificationRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.strategies.PaymentStrategy;
import org.da_scegliere.progetto_ids_hackathon.application.ports.strategies.exceptions.PaymentProviderException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.payment.PaymentFailedException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.payment.WinnerNotProclaimedException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.hackathon.HackathonNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.Hackathon;
import org.da_scegliere.progetto_ids_hackathon.core.entities.notification.BaseNotification;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Team;
import org.da_scegliere.progetto_ids_hackathon.core.entities.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Application service responsible for winner prize disbursement orchestration.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentStrategy paymentStrategy;
    private final IHackathonRepository hackathonRepository;
    private final Clock clock;
    private final INotificationRepository notificationRepository;

    /**
     * Awards the prize to a winner by resolving hackathon identifier.
     *
     * @param prize prize amount.
     * @param hackathonId hackathon identifier.
     * @return {@code true} when payment executed, {@code false} when already paid.
     */
    @Transactional
    public boolean awardPrizeToWinner(BigDecimal prize, UUID hackathonId) {
        if (hackathonId == null) {
            throw new IllegalArgumentException("hackathonId must not be null.");
        }
        Hackathon hackathon = hackathonRepository.findByIdForUpdate(hackathonId)
                .orElseThrow(() -> new HackathonNotFoundException(hackathonId));
        return awardPrizeToWinner(prize, hackathon);
    }

    /**
     * Awards the prize to the proclaimed winner and guarantees idempotency.
     *
     * @param prize prize amount to transfer to the winner team.
     * @param hackathon hackathon aggregate containing winner and payment status.
     * @return {@code true} when a payment has been executed in this call,
     *         {@code false} when the prize had already been paid before.
     * @throws IllegalArgumentException when prize or hackathon input is invalid.
     * @throws WinnerNotProclaimedException when no winner has been proclaimed yet.
     * @throws PaymentFailedException when the payment provider fails to execute the payment.
     */
    @Transactional
    public boolean awardPrizeToWinner(BigDecimal prize, Hackathon hackathon) {
        validatePrize(prize);
        Hackathon safeHackathon = validateHackathon(hackathon);
        log.info("Awarding winner prize for hackathonId={}.", safeHackathon.getId());

        Team winner = safeHackathon.getWinner();
        if (winner == null) {
            throw new WinnerNotProclaimedException();
        }
        if (safeHackathon.isPrizeAlreadyPaid()) {
            return false;
        }
        for(User UserToNotify : winner.getMembers()){
            BaseNotification notification = new BaseNotification("Pagamento effettuato", "il pagamento é stato effettuato", UserToNotify, 3);
            notificationRepository.save(notification);
        }
        executePayment(prize, winner);
        safeHackathon.markPrizeAsPaid(LocalDate.now(clock));
        log.info("Awarded winner prize={} for hackathonId={}.", prize, safeHackathon.getId());
        return true;
    }

    private static void validatePrize(BigDecimal prize) {
        if (prize == null || prize.signum() <= 0) {
            throw new IllegalArgumentException("prize must be a positive value.");
        }
    }

    private static void validateTeam(Team team) {
        if (team == null) {
            throw new IllegalArgumentException("team must not be null.");
        }
    }

    private static Hackathon validateHackathon(Hackathon hackathon) {
        if (hackathon == null) {
            throw new IllegalArgumentException("hackathon must not be null.");
        }
        return hackathon;
    }

    private void executePayment(BigDecimal prize, Team team) {
        validateTeam(team);
        try {
            paymentStrategy.awardPrize(prize, team);
        } catch (PaymentProviderException ex) {
            throw new PaymentFailedException("Payment provider failed to award the prize.", ex);
        } catch (RuntimeException ex) {
            throw new PaymentFailedException("Unexpected payment provider error while awarding prize.", ex);
        }
    }
}

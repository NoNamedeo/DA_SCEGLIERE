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

import org.da_scegliere.progetto_ids_hackathon.application.ports.strategies.PaymentStrategy;
import org.da_scegliere.progetto_ids_hackathon.application.ports.strategies.exceptions.PaymentProviderException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.payment.PaymentFailedException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.payment.WinnerNotProclaimedException;
import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.Hackathon;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Team;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Application service responsible for winner prize disbursement orchestration.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Validate payment preconditions at application level.</li>
 *     <li>Invoke the configured payment strategy for provider integration.</li>
 *     <li>Map provider errors to stable application exceptions.</li>
 *     <li>Enforce idempotency by checking and recording payment status on the hackathon aggregate.</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentStrategy paymentStrategy;
    private final Clock clock;

    /**
     * Creates a new service instance.
     *
     * @param paymentStrategy strategy adapter used to perform prize disbursement.
     * @param clock application clock used to timestamp successful payments.
     * @throws NullPointerException when any dependency is {@code null}.
     */
    public PaymentService(PaymentStrategy paymentStrategy, Clock clock) {
        this.paymentStrategy = Objects.requireNonNull(paymentStrategy, "paymentStrategy must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
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

        Team winner = safeHackathon.getWinner();
        if (winner == null) {
            throw new WinnerNotProclaimedException();
        }
        if (safeHackathon.isPrizeAlreadyPaid()) {
            return false;
        }

        executePayment(prize, winner);
        safeHackathon.markPrizeAsPaid(LocalDate.now(clock));
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

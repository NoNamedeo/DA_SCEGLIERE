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

package org.da_scegliere.progetto_ids_hackathon.infrastructure;

import org.da_scegliere.progetto_ids_hackathon.application.ports.strategies.PaymentStrategy;
import org.da_scegliere.progetto_ids_hackathon.application.ports.strategies.exceptions.PaymentProviderException;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Team;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Primary
public class PayPalPaymentStrategy implements PaymentStrategy {

    /**
     * TODO: integrate PayPal API.
     */
    @Override
    public void awardPrize(BigDecimal prize, Team team) {
        if (prize == null || prize.signum() <= 0) {
            throw new IllegalArgumentException("prize must be a positive amount.");
        }
        if (team == null) {
            throw new IllegalArgumentException("team must not be null.");
        }
        try {
            // Integration point with external payment provider.
        } catch (RuntimeException ex) {
            throw new PaymentProviderException("Failed to process payment through PayPal.", ex);
        }
    }
}

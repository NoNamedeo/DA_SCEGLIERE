/*
 * Authors:  Alejandro Innocenzi, Matteo Vittori
 * Copyright (c) 2026 Alejandro Innocenzi, Matteo Vittori. All rights reserved.
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

package org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.builder;

import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.HackathonTimeline;

import java.time.LocalDate;
import java.util.Objects;

public class HackathonBuilderDirector {

    /**
     * Automatic BuilderDirector that builds a hackathon with some
     * specific short deadlines based on a provided reference date:
     * RegistrationDeadline: +20 days
     * SubmissionDeadline: +65 days
     * EvaluationDeadline: +85 days
     * PrizePaidAt: +90 days
     *
     * @param builder
     * @param referenceDate reference date used to compute deadlines
     * @return IHackathonBuilder
     */
    public static IHackathonBuilder makeShortHackathonDeadlines(HackathonBuilder builder, LocalDate referenceDate){
        LocalDate safeReferenceDate = Objects.requireNonNull(referenceDate, "referenceDate must not be null.");
        return builder
                .setTimeline(new HackathonTimeline(
                        safeReferenceDate.plusDays(20),
                        safeReferenceDate.plusDays(65),
                        safeReferenceDate.plusDays(85)
                ))
                .setPrizePaidAt(safeReferenceDate.plusDays(90));
    }

    /**
     * Automatic BuilderDirector that builds a hackathon with some
     * specific long deadlines based on a provided reference date:
     * RegistrationDeadline: +50 days
     * SubmissionDeadline: +150 days
     * EvaluationDeadline: +175 days
     * PrizePaidAt: +190 days
     *
     * @param builder
     * @param referenceDate reference date used to compute deadlines
     * @return IHackathonBuilder
     */
    public static IHackathonBuilder makeLongHackathonDeadlines(HackathonBuilder builder, LocalDate referenceDate){
        LocalDate safeReferenceDate = Objects.requireNonNull(referenceDate, "referenceDate must not be null.");
        return builder
                .setTimeline(new HackathonTimeline(
                        safeReferenceDate.plusDays(50),
                        safeReferenceDate.plusDays(150),
                        safeReferenceDate.plusDays(175)
                ))
                .setPrizePaidAt(safeReferenceDate.plusDays(190));
    }

}

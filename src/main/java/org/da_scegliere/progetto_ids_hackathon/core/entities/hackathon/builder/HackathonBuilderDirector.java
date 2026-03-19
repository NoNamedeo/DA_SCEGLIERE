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

package org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.builder;

import java.time.LocalDate;

public class HackathonBuilderDirector {

    /**
     * Automatic BuilderDirector that builds a hackathon with some
     * specific short deadlines; which are (referring to LocalDate.now() as now):
     * RegistrationDeadline: 20 days from now
     * SubmissionDeadline: 65 days from now
     * EvaluationDeadline: 85 days from now
     * PrizePaidAt: 90 days from now
     *
     * @param builder
     * @return IHackathonBuilder
     */
    public static IHackathonBuilder makeShortHackathonDeadlines(HackathonBuilder builder){
        return builder
                .setRegistrationDeadline(LocalDate.now().plusDays(20))
                .setSubmissionDeadline(LocalDate.now().plusDays(65))
                .setEvaluationDeadline(LocalDate.now().plusDays(85))
                .setPrizePaidAt(LocalDate.now().plusDays(90));
    }

    /**
     * Automatic BuilderDirector that builds a hackathon with some
     * specific long deadlines; which are (referring to LocalDate.now() as now):
     * RegistrationDeadline: 50 days from now
     * SubmissionDeadline: 150 days from now
     * EvaluationDeadline: 175 days from now
     * PrizePaidAt: 190 days from now
     *
     * @param builder
     * @return IHackathonBuilder
     */
    public static IHackathonBuilder makeLongHackathonDeadlines(HackathonBuilder builder){
        return builder
                .setRegistrationDeadline(LocalDate.now().plusDays(50))
                .setSubmissionDeadline(LocalDate.now().plusDays(150))
                .setEvaluationDeadline(LocalDate.now().plusDays(175))
                .setPrizePaidAt(LocalDate.now().plusDays(190));
    }

}

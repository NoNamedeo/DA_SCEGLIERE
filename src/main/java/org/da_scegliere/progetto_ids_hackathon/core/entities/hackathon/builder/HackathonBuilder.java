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

import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.Hackathon;
import org.da_scegliere.progetto_ids_hackathon.core.entities.staff.StaffAssignment;
import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.Participation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class HackathonBuilder implements IHackathonBuilder{

    private Hackathon hackathon = new Hackathon();

    @Override
    public Hackathon build() {
        Hackathon result = this.hackathon;
        this.reset();
        return result;
    }

    @Override
    public IHackathonBuilder reset() {
        this.hackathon = new Hackathon();
        return this;
    }

    @Override
    public IHackathonBuilder setName(String name) {
        this.hackathon.setName(name);
        return this;
    }

    @Override
    public IHackathonBuilder setDescription(String description) {
        this.hackathon.setDescription(description);
        return this;
    }

    @Override
    public IHackathonBuilder setParticipations(List<Participation> participations) {
        this.hackathon.setParticipations(participations);
        return this;
    }

    @Override
    public IHackathonBuilder setAwardPrize(BigDecimal prize) {
        this.hackathon.setAwardPrize(prize);
        return this;
    }

    @Override
    public IHackathonBuilder setPrizePaidAt(LocalDate date) {
        this.hackathon.setPrizePaidAt(date);
        return this;
    }

    @Override
    public IHackathonBuilder setStaff(List<StaffAssignment> staff) {
        this.hackathon.setStaff(staff);
        return this;
    }

    @Override
    public IHackathonBuilder setRegistrationDeadline(LocalDate date) {
        this.hackathon.setRegistrationDeadline(date);
        return this;
    }

    @Override
    public IHackathonBuilder setEvaluationDeadline(LocalDate date) {
        this.hackathon.setEvaluationDeadline(date);
        return this;
    }

    @Override
    public IHackathonBuilder setSubmissionDeadline(LocalDate date) {
        this.hackathon.setSubmissionDeadline(date);
        return this;
    }
}

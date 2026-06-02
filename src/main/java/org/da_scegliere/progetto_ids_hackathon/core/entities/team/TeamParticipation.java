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

package org.da_scegliere.progetto_ids_hackathon.core.entities.team;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.Participation;
import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.Hackathon;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Entity
public class TeamParticipation extends Participation {

    @NotNull
    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @OneToMany(
            mappedBy = "teamParticipation",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Submission> submissions = new ArrayList<>();

    private boolean disqualified;

    private LocalDate disqualifiedAt;

    private String disqualificationReason;

    public TeamParticipation(LocalDate entryDate, String nickName, Hackathon hackathon, Team team, List<Submission> submissions) {
        super(entryDate, nickName, hackathon);
        this.team = team;
        this.submissions = submissions;
        this.disqualified = false;
        this.disqualifiedAt = null;
        this.disqualificationReason = null;
    }

    public TeamParticipation() {
        super();
    }

    public void addSubmission(Submission submission) {
        if (submission != null && !submissions.contains(submission)) {
            submissions.add(submission);
            submission.setTeamParticipation(this);
        }
    }

    public void removeSubmission(Submission submission) {
        if (submission != null && submissions.remove(submission)) {
            submission.setTeamParticipation(null);
        }
    }

    public void disqualify(String reason, LocalDate referenceDate) {
        if (disqualified) {
            throw new IllegalStateException("Team participation is already disqualified.");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason must not be blank.");
        }
        this.disqualified = true;
        this.disqualifiedAt = Objects.requireNonNull(referenceDate, "referenceDate must not be null.");
        this.disqualificationReason = reason.trim();
    }
}

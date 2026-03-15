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

package org.da_scegliere.progetto_ids_hackathon.core.entities.team;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Getter
@Entity
public class Submission {
    private static final int MIN_SCORE = 0;
    private static final int MAX_SCORE = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @PastOrPresent
    private LocalDate submittedAt;

    @NotNull
    private String description;

    @NotEmpty
    private String title;

    private Integer judgeScore;

    private String judgeJudgement;

    @PastOrPresent
    private LocalDate evaluatedAt;

    @ManyToOne
    @JoinColumn(name = "team_participation_id")
    @Setter
    private TeamParticipation teamParticipation;

    public Submission(LocalDate date, String description, String title, TeamParticipation teamParticipation) {
        this.submittedAt = date;
        updateContent(title, description);
        this.teamParticipation = teamParticipation;
    }

    public Submission() {}

    public void updateContent(String title, String description) {
        this.title = requireText(title, "title");
        this.description = requireText(description, "description");
    }

    public void evaluate(int score, String judgement, LocalDate evaluationDate) {
        validateScore(score);
        this.judgeJudgement = requireText(judgement, "judgeJudgement");
        this.judgeScore = score;
        this.evaluatedAt = Objects.requireNonNull(evaluationDate, "evaluationDate must not be null");
    }

    public boolean hasEvaluation() {
        return judgeScore != null;
    }

    private static void validateScore(int score) {
        if (score < MIN_SCORE || score > MAX_SCORE) {
            throw new IllegalArgumentException("score must be between 0 and 10.");
        }
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
        return value;
    }
}

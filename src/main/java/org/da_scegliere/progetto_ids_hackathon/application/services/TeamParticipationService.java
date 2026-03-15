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

import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.ITeamParticipationRepository;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.hackathon.InvalidHackathonStateOperationException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.teamParticipation.InvalidSubmissionEvaluationException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.teamParticipation.SubmissionDeadlineExceededException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.teamParticipation.SubmissionEvaluationNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.teamParticipation.SubmissionNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.teamParticipation.TeamParticipationNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.Hackathon;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Submission;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.TeamParticipation;
import org.da_scegliere.progetto_ids_hackathon.core.enums.states.hackathon.HackathonState;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Application service focused on submission lifecycle within a team participation.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Handle submission CRUD-like operations for teams enrolled in hackathons.</li>
 *     <li>Apply temporal and state business constraints for submission/evaluation actions.</li>
 *     <li>Validate judge evaluations according to UC rules (score range and textual judgement).</li>
 *     <li>Raise explicit application exceptions for invalid operations and missing entities.</li>
 * </ul>
 */
@Transactional(readOnly = true)
public class TeamParticipationService {
    private static final int MIN_JUDGE_SCORE = 0;
    private static final int MAX_JUDGE_SCORE = 10;

    private static final String OP_CREATE_SUBMISSION = "Create submission";
    private static final String OP_UPDATE_SUBMISSION = "Update submission";
    private static final String OP_EVALUATE_SUBMISSION = "Evaluate submission";
    private static final String OP_UPDATE_SUBMISSION_EVALUATION = "Update submission evaluation";

    private final ITeamParticipationRepository teamParticipationRepository;

    /**
     * Creates a new service instance.
     *
     * @param teamParticipationRepository repository for team-participation aggregate persistence and lookups.
     * @throws NullPointerException when {@code teamParticipationRepository} is {@code null}.
     */
    public TeamParticipationService(ITeamParticipationRepository teamParticipationRepository) {
        this.teamParticipationRepository = Objects.requireNonNull(
                teamParticipationRepository,
                "teamParticipationRepository must not be null"
        );
    }

    /**
     * Retrieves a team participation by id.
     *
     * @param teamParticipationId participation identifier.
     * @return resolved team participation aggregate.
     * @throws IllegalArgumentException when {@code teamParticipationId} is {@code null}.
     * @throws TeamParticipationNotFoundException when participation does not exist.
     */
    public TeamParticipation getTeamParticipationById(UUID teamParticipationId) {
        if (teamParticipationId == null) {
            throw new IllegalArgumentException("teamParticipationId must not be null.");
        }

        return teamParticipationRepository
                .findById(teamParticipationId)
                .orElseThrow(() -> new TeamParticipationNotFoundException(teamParticipationId));
    }

    /**
     * Retrieves a submission by id.
     *
     * @param submissionId submission identifier.
     * @return resolved submission.
     * @throws IllegalArgumentException when {@code submissionId} is {@code null}.
     * @throws SubmissionNotFoundException when submission cannot be resolved.
     */
    public Submission getSubmissionById(UUID submissionId) {
        return resolveSubmissionContext(submissionId).submission();
    }

    /**
     * Creates a new submission for a team participation.
     * <p>
     * Operation allowed only during hackathon {@code ONGOING} phase and before submission deadline.
     *
     * @param teamParticipationId participation identifier.
     * @param title submission title.
     * @param description submission description.
     * @return created submission instance attached to the participation.
     * @throws IllegalArgumentException when mandatory input fields are blank/null.
     * @throws TeamParticipationNotFoundException when participation does not exist.
     * @throws InvalidHackathonStateOperationException when hackathon state does not allow the operation.
     * @throws SubmissionDeadlineExceededException when deadline has passed.
     */
    @Transactional
    public Submission createSubmission(UUID teamParticipationId, String title, String description) {
        TeamParticipation participation = getTeamParticipationById(teamParticipationId);
        validateSubmissionWindow(participation, OP_CREATE_SUBMISSION);
        validateSubmissionContent(title, description);

        Submission submission = new Submission(
                LocalDate.now(),
                description,
                title,
                null
        );
        participation.addSubmission(submission);
        return submission;
    }

    /**
     * Updates title and description of an existing submission.
     * <p>
     * Operation allowed only during hackathon {@code ONGOING} phase and before submission deadline.
     *
     * @param submissionId submission identifier.
     * @param newTitle new submission title.
     * @param newDescription new submission description.
     * @return updated submission.
     * @throws IllegalArgumentException when mandatory input fields are blank/null.
     * @throws SubmissionNotFoundException when submission does not exist.
     * @throws InvalidHackathonStateOperationException when hackathon state does not allow the operation.
     * @throws SubmissionDeadlineExceededException when deadline has passed.
     */
    @Transactional
    public Submission updateSubmission(UUID submissionId, String newTitle, String newDescription) {
        SubmissionContext context = resolveSubmissionContext(submissionId);
        validateSubmissionWindow(context.teamParticipation(), OP_UPDATE_SUBMISSION);
        validateSubmissionContent(newTitle, newDescription);

        context.submission().updateContent(newTitle, newDescription);
        return context.submission();
    }

    /**
     * Evaluates a submission with score and textual judgement.
     * <p>
     * Operation allowed only in hackathon {@code EVALUATION} phase.
     *
     * @param submissionId submission identifier.
     * @param score judge score in range [0, 10].
     * @param judgement textual judgement provided by the judge.
     * @return evaluated submission.
     * @throws InvalidSubmissionEvaluationException when score/judgement input is invalid.
     * @throws SubmissionNotFoundException when submission does not exist.
     * @throws InvalidHackathonStateOperationException when hackathon state does not allow the operation.
     */
    @Transactional
    public Submission evaluateSubmission(UUID submissionId, int score, String judgement) {
        SubmissionContext context = resolveSubmissionContext(submissionId);
        validateEvaluationWindow(context.teamParticipation(), OP_EVALUATE_SUBMISSION);
        validateJudgeEvaluationInput(score, judgement);

        context.submission().evaluate(score, judgement, LocalDate.now());
        return context.submission();
    }

    /**
     * Updates an existing submission evaluation.
     * <p>
     * Operation allowed only in hackathon {@code EVALUATION} phase and only if the submission
     * has already been evaluated.
     *
     * @param submissionId submission identifier.
     * @param score updated score in range [0, 10].
     * @param judgement updated textual judgement.
     * @return submission with updated evaluation.
     * @throws SubmissionEvaluationNotFoundException when no previous evaluation exists.
     * @throws InvalidSubmissionEvaluationException when score/judgement input is invalid.
     * @throws SubmissionNotFoundException when submission does not exist.
     * @throws InvalidHackathonStateOperationException when hackathon state does not allow the operation.
     */
    @Transactional
    public Submission updateSubmissionEvaluation(UUID submissionId, int score, String judgement) {
        SubmissionContext context = resolveSubmissionContext(submissionId);
        validateEvaluationWindow(context.teamParticipation(), OP_UPDATE_SUBMISSION_EVALUATION);

        if (!context.submission().hasEvaluation()) {
            throw new SubmissionEvaluationNotFoundException(submissionId);
        }
        validateJudgeEvaluationInput(score, judgement);

        context.submission().evaluate(score, judgement, LocalDate.now());
        return context.submission();
    }

    /**
     * Deletes a submission from its team participation.
     *
     * @param submissionId submission identifier.
     * @throws SubmissionNotFoundException when submission does not exist.
     */
    @Transactional
    public void deleteSubmission(UUID submissionId) {
        SubmissionContext context = resolveSubmissionContext(submissionId);
        context.teamParticipation().removeSubmission(context.submission());
    }

    /**
     * Adds an already instantiated submission to a participation.
     * <p>
     * Operation allowed only during hackathon {@code ONGOING} phase and before submission deadline.
     *
     * @param submission submission instance to attach.
     * @param teamParticipationId participation identifier.
     * @throws IllegalArgumentException when input is invalid.
     * @throws TeamParticipationNotFoundException when participation does not exist.
     * @throws InvalidHackathonStateOperationException when hackathon state does not allow the operation.
     * @throws SubmissionDeadlineExceededException when deadline has passed.
     */
    @Transactional
    public void addSubmissionTo(Submission submission, UUID teamParticipationId) {
        if (submission == null) {
            throw new IllegalArgumentException("submission must not be null.");
        }

        TeamParticipation participation = getTeamParticipationById(teamParticipationId);
        validateSubmissionWindow(participation, OP_CREATE_SUBMISSION);
        validateSubmissionContent(submission.getTitle(), submission.getDescription());

        participation.addSubmission(submission);
    }

    private SubmissionContext resolveSubmissionContext(UUID submissionId) {
        if (submissionId == null) {
            throw new IllegalArgumentException("submissionId must not be null.");
        }

        TeamParticipation participation = teamParticipationRepository
                .findBySubmissions_id(submissionId)
                .orElseThrow(() -> new SubmissionNotFoundException(submissionId));

        Submission submission = findSubmissionById(participation, submissionId);
        return new SubmissionContext(participation, submission);
    }

    private static void validateSubmissionContent(String title, String description) {
        requireNonBlank(title, "title");
        requireNonBlank(description, "description");
    }

    private static void validateJudgeEvaluationInput(int score, String judgement) {
        if (score < MIN_JUDGE_SCORE || score > MAX_JUDGE_SCORE) {
            throw new InvalidSubmissionEvaluationException("score must be between 0 and 10.");
        }
        requireNonBlank(judgement, "judgement");
    }

    private static void requireNonBlank( String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }

    private void validateSubmissionWindow(TeamParticipation participation, String operationName) {
        Hackathon hackathon = extractHackathon(participation);
        if (hackathon.getHackathonState() != HackathonState.ONGOING) {
            throw new InvalidHackathonStateOperationException(hackathon.getHackathonState(), operationName);
        }

        LocalDate submissionDeadline = hackathon.getSubmissionDeadline();
        if (submissionDeadline != null && LocalDate.now().isAfter(submissionDeadline)) {
            throw new SubmissionDeadlineExceededException(submissionDeadline);
        }
    }

    private void validateEvaluationWindow(TeamParticipation participation, String operationName) {
        Hackathon hackathon = extractHackathon(participation);
        if (hackathon.getHackathonState() != HackathonState.EVALUATION) {
            throw new InvalidHackathonStateOperationException(hackathon.getHackathonState(), operationName);
        }
    }

    private static Hackathon extractHackathon(TeamParticipation participation) {
        Objects.requireNonNull(participation, "teamParticipation must not be null.");
        Hackathon hackathon = participation.getHackathon();
        if (hackathon == null) {
            throw new IllegalStateException("Team participation is not linked to any hackathon.");
        }
        return hackathon;
    }

    private Submission findSubmissionById(TeamParticipation teamParticipation, UUID submissionId) {
        return teamParticipation.getSubmissions()
                .stream()
                .filter(s -> submissionId.equals(s.getId()))
                .findFirst()
                .orElseThrow(() -> new SubmissionNotFoundException(submissionId));
    }

    private record SubmissionContext(TeamParticipation teamParticipation, Submission submission) { }
}

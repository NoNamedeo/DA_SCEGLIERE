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
import org.da_scegliere.progetto_ids_hackathon.core.enums.state.hackathon.HackathonState;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Application service focused on submission lifecycle within a team participation.
 * <p>
 */
@Slf4j
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class TeamParticipationService {

    private static final String OP_CREATE_SUBMISSION = "Create submission";
    private static final String OP_UPDATE_SUBMISSION = "Update submission";
    private static final String OP_EVALUATE_SUBMISSION = "Evaluate submission";
    private static final String OP_UPDATE_SUBMISSION_EVALUATION = "Update submission evaluation";

    private final ITeamParticipationRepository teamParticipationRepository;
    private final Clock clock;


    /**
     * Retrieves a team participation by id.
     *
     * @param teamParticipationId participation identifier.
     * @return resolved team participation aggregate.
     * @throws IllegalArgumentException when {@code teamParticipationId} is {@code null}.
     * @throws TeamParticipationNotFoundException when participation does not exist.
     */
    public TeamParticipation getTeamParticipationById(UUID teamParticipationId) {
        log.info("Get team participation teamParticipationId={}", teamParticipationId);

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
        log.info("Get submission submissionId={}", submissionId);

        return resolveSubmissionContext(submissionId).submission();
    }

    /**
     * Retrieves a list of submissions by a teamParticipation identifier.
     *
     * @param teamParticipationId teamParticipation identifier.
     * @return resolved submission.
     * @throws IllegalArgumentException when {@code teamParticipationId} is {@code null}.
     */
    public List<Submission> getSubmissionsByTeamParticipation(UUID teamParticipationId) {
        log.info("Get submissions teamParticipationId={}", teamParticipationId);
        TeamParticipation teamParticipation = getTeamParticipationById(teamParticipationId);
        return teamParticipation.getSubmissions();
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
        log.info("Create submission teamParticipationId={}, title={}", teamParticipationId, title);

        TeamParticipation participation = getTeamParticipationById(teamParticipationId);
        LocalDate today = LocalDate.now(clock);
        validateSubmissionWindow(participation, OP_CREATE_SUBMISSION, today);
        validateSubmissionContent(title, description);

        Submission submission = new Submission(
                today,
                description,
                title,
                null
        );
        participation.addSubmission(submission);

        log.info("Created submission submissionId={}", submission.getId());
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
        log.info("Update submission submissionId={}", submissionId);

        SubmissionContext context = resolveSubmissionContext(submissionId);
        LocalDate today = LocalDate.now(clock);
        validateSubmissionWindow(context.teamParticipation(), OP_UPDATE_SUBMISSION, today);
        validateSubmissionContent(newTitle, newDescription);

        context.submission().updateContent(newTitle, newDescription);

        log.info("Updated submission submissionId={}", submissionId);
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
    public Submission evaluateSubmission(UUID submissionId, Integer score, String judgement) {
        log.info("Evaluate submission submissionId={}", submissionId);

        SubmissionContext context = resolveSubmissionContext(submissionId);
        LocalDate today = LocalDate.now(clock);
        validateEvaluationWindow(context.teamParticipation(), OP_EVALUATE_SUBMISSION, today);

        context.submission().evaluate(score, judgement, today);

        log.info("Evaluated submission submissionId={}", submissionId);
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
        log.info("Update submission evaluation submissionId={}", submissionId);

        SubmissionContext context = resolveSubmissionContext(submissionId);
        LocalDate today = LocalDate.now(clock);
        validateEvaluationWindow(context.teamParticipation(), OP_UPDATE_SUBMISSION_EVALUATION, today);

        if (!context.submission().hasEvaluation()) {
            throw new SubmissionEvaluationNotFoundException(submissionId);
        }

        context.submission().evaluate(score, judgement, today);

        log.info("Updated submission evaluation submissionId={}", submissionId);
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
        log.info("Delete submission submissionId={}", submissionId);

        SubmissionContext context = resolveSubmissionContext(submissionId);
        context.teamParticipation().removeSubmission(context.submission());

        log.info("Deleted submission submissionId={}", submissionId);
    }

    /**
     * Adds an already instantiated submission to a participation.
     * <p>
     * Operation allowed only during hackathon {@code ONGOING} phase and before submission deadline.
     *
     * @param submissionId submission instance to attach.
     * @param teamParticipationId participation identifier.
     * @throws IllegalArgumentException when input is invalid.
     * @throws TeamParticipationNotFoundException when participation does not exist.
     * @throws InvalidHackathonStateOperationException when hackathon state does not allow the operation.
     * @throws SubmissionDeadlineExceededException when deadline has passed.
     */
    @Transactional
    public void addSubmissionTo(UUID submissionId, UUID teamParticipationId) {
        log.info("Add submission submissionId={} to team participation teamParticipationId={}", submissionId, teamParticipationId);

        Submission submission = getSubmissionById(submissionId);

        TeamParticipation participation = getTeamParticipationById(teamParticipationId);
        validateSubmissionWindow(participation, OP_CREATE_SUBMISSION, LocalDate.now(clock));
        validateSubmissionContent(submission.getTitle(), submission.getDescription());

        participation.addSubmission(submission);
        log.info("Added submission submissionId={} to team participation teamParticipationId={}", submissionId, teamParticipationId);
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

    private static void requireNonBlank( String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }

    private void validateSubmissionWindow(TeamParticipation participation, String operationName, LocalDate referenceDate) {
        Hackathon hackathon = extractHackathon(participation);
        HackathonState currentState = hackathon.getHackathonStateAt(referenceDate);
        if (currentState != HackathonState.ONGOING) {
            throw new InvalidHackathonStateOperationException(currentState, operationName);
        }

        LocalDate submissionDeadline = hackathon.getSubmissionDeadline();
        if (submissionDeadline != null && referenceDate.isAfter(submissionDeadline)) {
            throw new SubmissionDeadlineExceededException(submissionDeadline);
        }
    }

    private void validateEvaluationWindow(TeamParticipation participation, String operationName, LocalDate referenceDate) {
        Hackathon hackathon = extractHackathon(participation);
        HackathonState currentState = hackathon.getHackathonStateAt(referenceDate);
        if (currentState != HackathonState.EVALUATION) {
            throw new InvalidHackathonStateOperationException(currentState, operationName);
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

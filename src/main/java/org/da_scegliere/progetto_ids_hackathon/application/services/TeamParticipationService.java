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

package org.da_scegliere.progetto_ids_hackathon.application.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IHackathonRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.ITeamParticipationRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.ITeamRepository;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.hackathon.HackathonNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.hackathon.InvalidHackathonStateOperationException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.team.TeamNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.teamParticipation.InvalidSubmissionEvaluationException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.teamParticipation.SubmissionDeadlineExceededException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.teamParticipation.SubmissionEvaluationNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.teamParticipation.SubmissionNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.teamParticipation.TeamAlreadyParticipatingException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.teamParticipation.TeamParticipationAlreadyDisqualifiedException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.teamParticipation.TeamParticipationNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.Hackathon;
import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.HackathonTimeline;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Submission;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Team;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.TeamParticipation;
import org.da_scegliere.progetto_ids_hackathon.core.enums.state.hackathon.HackathonState;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
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

    private static final String OP_CREATE_TEAM_PARTICIPATION = "Create team participation";
    private static final String OP_CREATE_SUBMISSION = "Create submission";
    private static final String OP_UPDATE_SUBMISSION = "Update submission";
    private static final String OP_EVALUATE_SUBMISSION = "Evaluate submission";
    private static final String OP_UPDATE_SUBMISSION_EVALUATION = "Update submission evaluation";

    private final ITeamParticipationRepository teamParticipationRepository;
    private final ITeamRepository teamRepository;
    private final IHackathonRepository hackathonRepository;
    private final Clock clock;


    public List<TeamParticipation> getAllTeamParticipationsByTeamId(UUID teamId) {
        log.info("Getting all team participations for teamId={}.", teamId);

        if(teamId == null){
            throw new IllegalArgumentException("teamId must not be null.");
        }

        return List.copyOf(teamParticipationRepository.findByTeam_id(teamId));
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
        log.info("Getting team participation teamParticipationId={}", teamParticipationId);

        if (teamParticipationId == null) {
            throw new IllegalArgumentException("teamParticipationId must not be null.");
        }

        return teamParticipationRepository
                .findById(teamParticipationId)
                .orElseThrow(() -> new TeamParticipationNotFoundException(teamParticipationId));
    }

    /**
     * Creates and persists a team participation for a given hackathon and team.
     * <p>
     * Operation allowed only during hackathon {@code REGISTRATION} phase.
     *
     * @param hackathonId target hackathon identifier.
     * @param teamId team identifier.
     * @param nickname participation nickname.
     * @return persisted team participation.
     * @throws IllegalArgumentException when input is invalid.
     * @throws HackathonNotFoundException when hackathon does not exist.
     * @throws TeamNotFoundException when team does not exist.
     * @throws InvalidHackathonStateOperationException when hackathon state does not allow new participations.
     * @throws TeamAlreadyParticipatingException when the team is already registered in the same hackathon.
     */
    @Transactional
    public TeamParticipation createTeamParticipation(UUID hackathonId, UUID teamId, String nickname) {
        log.info("Creating team participation hackathonId={} teamId={}", hackathonId, teamId);

        if (hackathonId == null) {
            throw new IllegalArgumentException("hackathonId must not be null.");
        }
        if (teamId == null) {
            throw new IllegalArgumentException("teamId must not be null.");
        }
        requireNonBlank(nickname, "nickname");

        Hackathon hackathon = hackathonRepository.findById(hackathonId)
                .orElseThrow(() -> new HackathonNotFoundException(hackathonId));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(teamId));

        LocalDate today = LocalDate.now(clock);
        HackathonState currentState = hackathon.getHackathonStateAt(today);
        if (currentState != HackathonState.REGISTRATION) {
            throw new InvalidHackathonStateOperationException(currentState, OP_CREATE_TEAM_PARTICIPATION);
        }

        if (teamParticipationRepository.existsByHackathon_idAndTeam_id(hackathonId, teamId)) {
            throw new TeamAlreadyParticipatingException(teamId, hackathonId);
        }

        TeamParticipation participation = new TeamParticipation(
                today,
                nickname,
                hackathon,
                team,
                new ArrayList<>()
        );

        TeamParticipation savedParticipation = teamParticipationRepository.save(participation);
        log.info(
                "Created team participation participationId={} hackathonId={} teamId={}",
                savedParticipation.getId(),
                hackathonId,
                teamId
        );
        return savedParticipation;
    }

    /**
     * Retrieves a teams by hackathonId.
     *
     * @param hackathonId hackathon identifier.
     * @return list of participating teams.
     * @throws HackathonNotFoundException when hackathon does not exist.
     */
    public List<Team> getTeamsByHackathon( UUID hackathonId) {
        log.info("Getting teams by hackathonId={}", hackathonId);

        Hackathon hackathon = hackathonRepository.findById(hackathonId)
                .orElseThrow(() -> new HackathonNotFoundException(hackathonId));

        return teamParticipationRepository.findByHackathon_id(hackathon.getId())
                .stream()
                .map(TeamParticipation::getTeam)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Retrieves all submissions belonging to one hackathon.
     *
     * @param hackathonId hackathon identifier.
     * @return immutable snapshot of submissions.
     * @throws IllegalArgumentException when {@code hackathonId} is {@code null}.
     * @throws HackathonNotFoundException when hackathon does not exist.
     */
    public List<Submission> getSubmissionsByHackathon(UUID hackathonId) {
        log.info("Getting submissions by hackathonId={}", hackathonId);

        if (hackathonId == null) {
            throw new IllegalArgumentException("hackathonId must not be null.");
        }
        if (!hackathonRepository.existsById(hackathonId)) {
            throw new HackathonNotFoundException(hackathonId);
        }

        List<Submission> submissions = teamParticipationRepository.findByHackathon_id(hackathonId)
                .stream()
                .flatMap(participation -> participation.getSubmissions().stream())
                .toList();
        log.info("Retrieved {} submissions for hackathonId={}.", submissions.size(), hackathonId);
        return List.copyOf(submissions);
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
        log.debug("Retrieving submission submissionId={}", submissionId);

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
        log.debug("Retrieving submissions teamParticipationId={}", teamParticipationId);

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
        log.info("Creating submission teamParticipationId={}, title={}", teamParticipationId, title);

        TeamParticipation participation = getTeamParticipationById(teamParticipationId);
        ensureParticipationIsActive(participation);
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
        log.info("Updating submission submissionId={}", submissionId);

        SubmissionContext context = resolveSubmissionContext(submissionId);
        ensureParticipationIsActive(context.teamParticipation());
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
        log.info("Evaluating submission submissionId={}, score={}", submissionId, score);

        SubmissionContext context = resolveSubmissionContext(submissionId);
        ensureParticipationIsActive(context.teamParticipation());
        LocalDate today = LocalDate.now(clock);
        validateEvaluationWindow(context.teamParticipation(), OP_EVALUATE_SUBMISSION, today);

        context.submission().evaluate(score, judgement, today);

        log.info("Evaluated submission submissionId={}, score={}, judgement={}", submissionId, score, judgement);
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
    public Submission updateSubmissionEvaluation(UUID submissionId, Integer score, String judgement) {
        log.info("Updating submission evaluation submissionId={}, score={}", submissionId, score);

        SubmissionContext context = resolveSubmissionContext(submissionId);
        ensureParticipationIsActive(context.teamParticipation());
        LocalDate today = LocalDate.now(clock);
        validateEvaluationWindow(context.teamParticipation(), OP_UPDATE_SUBMISSION_EVALUATION, today);

        if (!context.submission().hasEvaluation()) {
            throw new SubmissionEvaluationNotFoundException(submissionId);
        }

        context.submission().evaluate(score, judgement, today);

        log.info("Updated submission evaluation submissionId={}, score={}", submissionId, score);
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
        log.info("Deleting submission submissionId={}", submissionId);

        SubmissionContext context = resolveSubmissionContext(submissionId);
        ensureParticipationIsActive(context.teamParticipation());
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
        log.info("Adding submission submissionId={} to team participation teamParticipationId={}", submissionId, teamParticipationId);

        Submission submission = getSubmissionById(submissionId);

        TeamParticipation participation = getTeamParticipationById(teamParticipationId);
        ensureParticipationIsActive(participation);
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

    private static void ensureParticipationIsActive(TeamParticipation participation) {
        if (participation.isDisqualified()) {
            throw new TeamParticipationAlreadyDisqualifiedException(participation.getId());
        }
    }

    private void validateSubmissionWindow(TeamParticipation participation, String operationName, LocalDate referenceDate) {
        Hackathon hackathon = extractHackathon(participation);
        HackathonState currentState = hackathon.getHackathonStateAt(referenceDate);
        if (currentState != HackathonState.ONGOING) {
            throw new InvalidHackathonStateOperationException(currentState, operationName);
        }

        HackathonTimeline timeline = hackathon.getTimeline();
        LocalDate submissionDeadline = timeline == null ? null : timeline.submissionDeadline();
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

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

package org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.da_scegliere.progetto_ids_hackathon.core.policies.BusinessPolicy;
import org.da_scegliere.progetto_ids_hackathon.core.policies.hackathon.winner.WinnerAssignmentContext;
import org.da_scegliere.progetto_ids_hackathon.core.entities.staff.StaffAssignment;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Submission;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Team;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.TeamParticipation;
import org.da_scegliere.progetto_ids_hackathon.core.enums.state.hackathon.HackathonState;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Getter
@Entity
public class Hackathon {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Setter
    private String name;

    @NotNull
    @Setter
    private String description;

    @OneToMany(mappedBy = "hackathon")
    @Setter
    private List<Participation> participations;

    @ManyToOne
    @JoinColumn( name = "winner_team_id")
    private Team winner;

    @NotNull
    @DecimalMin(value = "0.01")
    @Setter
    private BigDecimal awardPrize;
    /**
     * Non-null when the winner prize has already been paid.
     */
    @Setter
    private LocalDate prizePaidAt;

    @OneToMany(mappedBy = "hackathon")
    @Setter
    private List<StaffAssignment> staff;

    /**
     * End of registration phase (inclusive).
     * If null, registration is considered open until submission timeline starts.
     */
    @Setter
    private LocalDate registrationDeadline;

    /**
     * End of ongoing/submission phase (inclusive).
     * If null, ongoing phase has no temporal end configured.
     */
    @Setter
    private LocalDate submissionDeadline;

    /**
     * End of evaluation phase (inclusive).
     * If null, evaluation phase has no temporal end configured.
     */
    @Setter
    private LocalDate evaluationDeadline;

    public Hackathon(String name, String description, List<Participation> participations, List<StaffAssignment> staff, BigDecimal awardPrize) {
        this.name = name;
        this.description = description;
        this.participations = participations;
        this.staff = staff;
        this.awardPrize = awardPrize;
        this.winner = null;
        this.prizePaidAt = null;
        this.registrationDeadline = null;
        this.submissionDeadline = null;
        this.evaluationDeadline = null;
    }

    public Hackathon() {}

    /**
     * Returns lifecycle state derived from configured timeline and current date.
     */
    public HackathonState getHackathonState() {
        return getHackathonStateAt(LocalDate.now());
    }

    /**
     * Returns lifecycle state derived from configured timeline at a given reference date.
     *
     * @param referenceDate date used to resolve temporal phase.
     * @return resolved hackathon phase.
     */
    public HackathonState getHackathonStateAt(LocalDate referenceDate) {
        Objects.requireNonNull(referenceDate, "referenceDate must not be null.");
        validateTimelineOrThrow(registrationDeadline, submissionDeadline, evaluationDeadline);

        if (registrationDeadline != null && !referenceDate.isAfter(registrationDeadline)) {
            return HackathonState.REGISTRATION;
        }
        if (submissionDeadline == null) {
            return registrationDeadline == null
                    ? HackathonState.REGISTRATION
                    : HackathonState.ONGOING;
        }
        if (!referenceDate.isAfter(submissionDeadline)) {
            return HackathonState.ONGOING;
        }
        if (evaluationDeadline == null || !referenceDate.isAfter(evaluationDeadline)) {
            return HackathonState.EVALUATION;
        }
        return HackathonState.ENDED;
    }

    public void configureTimeline(
            LocalDate registrationDeadline,
            LocalDate submissionDeadline,
            LocalDate evaluationDeadline
    ) {
        validateTimelineOrThrow(registrationDeadline, submissionDeadline, evaluationDeadline);
        this.registrationDeadline = registrationDeadline;
        this.submissionDeadline = submissionDeadline;
        this.evaluationDeadline = evaluationDeadline;
    }

    public void setRegistrationDeadline(LocalDate registrationDeadline) {
        configureTimeline(registrationDeadline, this.submissionDeadline, this.evaluationDeadline);
    }

    public void setSubmissionDeadline(LocalDate submissionDeadline) {
        configureTimeline(this.registrationDeadline, submissionDeadline, this.evaluationDeadline);
    }

    public void setEvaluationDeadline(LocalDate evaluationDeadline) {
        configureTimeline(this.registrationDeadline, this.submissionDeadline, evaluationDeadline);
    }

    /**
     * Concludes the hackathon by forcing ENDED state at the provided reference date.
     *
     * @param referenceDate date used as temporal reference for conclusion.
     */
    public void concludeAt(LocalDate referenceDate) {
        Objects.requireNonNull(referenceDate, "referenceDate must not be null.");

        LocalDate endedThreshold = referenceDate.minusDays(1);
        LocalDate effectiveSubmissionDeadline = submissionDeadline;
        if (effectiveSubmissionDeadline == null || effectiveSubmissionDeadline.isAfter(endedThreshold)) {
            effectiveSubmissionDeadline = endedThreshold;
        }

        LocalDate effectiveRegistrationDeadline = registrationDeadline;
        if (effectiveRegistrationDeadline == null || effectiveRegistrationDeadline.isAfter(effectiveSubmissionDeadline)) {
            effectiveRegistrationDeadline = effectiveSubmissionDeadline;
        }

        configureTimeline(effectiveRegistrationDeadline, effectiveSubmissionDeadline, endedThreshold);
    }

    /**
     * Assigns winner by enforcing UC rules:
     * - only in EVALUATION phase
     * - winner team must participate in the hackathon
     * - all submissions must be evaluated
     */
    public void assignWinner(
            Team winnerTeam,
            LocalDate referenceDate,
            BusinessPolicy<WinnerAssignmentContext> winnerAssignmentPolicy
    ) {
        Objects.requireNonNull(winnerTeam, "winnerTeam must not be null.");
        Objects.requireNonNull(referenceDate, "referenceDate must not be null.");
        Objects.requireNonNull(winnerAssignmentPolicy, "winnerAssignmentPolicy must not be null.");
        if (winnerTeam.getId() == null) {
            throw new IllegalArgumentException("winnerTeam.id must not be null.");
        }

        List<TeamParticipation> teamParticipations = getTeamParticipations();
        List<Submission> submissions = collectSubmissions(teamParticipations);

        WinnerAssignmentContext context = new WinnerAssignmentContext(
                getHackathonStateAt(referenceDate),
                winnerTeam,
                teamParticipations,
                submissions
        );
        winnerAssignmentPolicy.validate(context);

        this.winner = winnerTeam;
    }

    public boolean isPrizeAlreadyPaid() {
        return prizePaidAt != null;
    }

    public void markPrizeAsPaid(LocalDate paymentDate) {
        Objects.requireNonNull(paymentDate, "paymentDate must not be null");
        if (this.prizePaidAt == null) {
            this.prizePaidAt = paymentDate;
        }
    }

    public void addStaffAssignment(StaffAssignment staffAssignment) {
        Objects.requireNonNull(staffAssignment, "staffAssignment must not be null.");
        if (staffAssignment.getStaffMember() == null || staffAssignment.getStaffMember().getId() == null) {
            throw new IllegalArgumentException("staffAssignment.staffMember.id must not be null.");
        }
        if (staffAssignment.getHackathon() != null && !Objects.equals(staffAssignment.getHackathon().getId(), this.id)) {
            throw new IllegalArgumentException("staffAssignment already belongs to another hackathon.");
        }

        ensureStaffCollectionInitialized();

        boolean alreadyAssigned = staff.stream()
                .anyMatch(existing -> sameStaffMember(existing, staffAssignment));
        if (alreadyAssigned) {
            throw new IllegalStateException("Staff member is already assigned to this hackathon.");
        }

        staffAssignment.setHackathon(this);
        staff.add(staffAssignment);
    }

    public int removeStaffAssignmentsByStaffMemberIds(Set<UUID> staffMemberIds) {
        Objects.requireNonNull(staffMemberIds, "staffMemberIds must not be null.");
        if (staffMemberIds.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("staffMemberIds must not contain null values.");
        }

        ensureStaffCollectionInitialized();
        List<StaffAssignment> remainingAssignments = new ArrayList<>();
        int removedCount = 0;

        for (StaffAssignment assignment : staff) {
            if (assignment == null) {
                continue;
            }
            UUID staffMemberId = assignment.getStaffMember() != null ? assignment.getStaffMember().getId() : null;
            boolean shouldRemove = staffMemberId != null && staffMemberIds.contains(staffMemberId);

            if (shouldRemove) {
                assignment.setHackathon(null);
                removedCount++;
                continue;
            }
            remainingAssignments.add(assignment);
        }

        this.staff = remainingAssignments;
        return removedCount;
    }


    private List<TeamParticipation> getTeamParticipations() {
        if (participations == null) {
            return Collections.emptyList();
        }
        return participations.stream()
                .filter(TeamParticipation.class::isInstance)
                .map(TeamParticipation.class::cast)
                .toList();
    }

    private static List<Submission> collectSubmissions(List<TeamParticipation> teamParticipations) {
        return teamParticipations.stream()
                .flatMap(participation -> {
                    List<Submission> participationSubmissions = participation.getSubmissions();
                    if (participationSubmissions == null) {
                        return Collections.<Submission>emptyList().stream();
                    }
                    return participationSubmissions.stream();
                })
                .toList();
    }

    private void ensureStaffCollectionInitialized() {
        if (staff == null) {
            staff = new ArrayList<>();
        }
    }

    private static boolean sameStaffMember(StaffAssignment first, StaffAssignment second) {
        if (first == null || second == null || first.getStaffMember() == null || second.getStaffMember() == null) {
            return false;
        }

        UUID firstId = first.getStaffMember().getId();
        UUID secondId = second.getStaffMember().getId();
        return firstId != null && Objects.equals(firstId, secondId);
    }

    private static void validateTimelineOrThrow(
            LocalDate registrationDeadline,
            LocalDate submissionDeadline,
            LocalDate evaluationDeadline
    ) {
        if (registrationDeadline != null
                && submissionDeadline != null
                && registrationDeadline.isAfter(submissionDeadline)) {
            throw new IllegalArgumentException("registrationDeadline must be on or before submissionDeadline.");
        }
        if (submissionDeadline == null && evaluationDeadline != null) {
            throw new IllegalArgumentException("evaluationDeadline requires submissionDeadline to be configured.");
        }
        if (submissionDeadline != null
                && evaluationDeadline != null
                && submissionDeadline.isAfter(evaluationDeadline)) {
            throw new IllegalArgumentException("submissionDeadline must be on or before evaluationDeadline.");
        }
    }
}

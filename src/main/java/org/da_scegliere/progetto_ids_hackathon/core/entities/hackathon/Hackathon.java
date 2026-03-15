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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.da_scegliere.progetto_ids_hackathon.core.entities.Participation;
import org.da_scegliere.progetto_ids_hackathon.core.entities.staff.StaffAssignment;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Submission;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Team;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.TeamParticipation;
import org.da_scegliere.progetto_ids_hackathon.core.enums.states.hackathon.HackathonState;

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

    @NotNull
    @Enumerated(EnumType.STRING)
    private HackathonState hackathonState;

    @NotBlank
    @Setter
    private String name;

    @NotNull
    @Setter
    private String description;

    @NotNull
    @OneToMany(mappedBy = "hackathon")
    @Setter
    private List<Participation> participations;

    @ManyToOne
    @JoinColumn( name = "winner_team_id")
    private Team winner;

    /**
     * Non-null when the winner prize has already been paid.
     */
    private LocalDate prizePaidAt;

    @NotEmpty
    @OneToMany(mappedBy = "hackathon")
    @Setter
    private List<StaffAssignment> staff;

    /**
     * Submission deadline (deadlineSottomissione).
     * If null, the temporal constraint is considered not configured.
     */
    @Setter
    private LocalDate submissionDeadline;

    public Hackathon(String name, String description, List<Participation> participations, List<StaffAssignment> staff) {
        this.name = name;
        this.description = description;
        this.participations = participations;
        this.staff = staff;
        this.hackathonState = HackathonState.REGISTRATION;
        this.winner = null;
        this.prizePaidAt = null;
        this.submissionDeadline = null;
    }

    public Hackathon() {}

    public void transitionTo(HackathonState targetState) {
        Objects.requireNonNull(targetState, "targetState must not be null");

        if (this.hackathonState == targetState) {
            return;
        }
        if (!isValidTransition(this.hackathonState, targetState)) {
            throw new IllegalStateException("Invalid state transition from " + this.hackathonState + " to " + targetState + ".");
        }

        this.hackathonState = targetState;
    }

    public void advanceState() {
        transitionTo(nextState(this.hackathonState));
    }

    /**
     * Assigns winner by enforcing UC rules:
     * - only in EVALUATION phase
     * - winner team must participate in the hackathon
     * - all submissions must be evaluated
     */
    public void assignWinner(Team winnerTeam) {
        Objects.requireNonNull(winnerTeam, "winnerTeam must not be null.");
        if (winnerTeam.getId() == null) {
            throw new IllegalArgumentException("winnerTeam.id must not be null.");
        }
        if (this.hackathonState != HackathonState.EVALUATION) {
            throw new IllegalStateException("Cannot assign winner outside EVALUATION state.");
        }

        List<TeamParticipation> teamParticipations = getTeamParticipations();
        boolean winnerParticipates = teamParticipations.stream()
                .map(TeamParticipation::getTeam)
                .anyMatch(team -> isSameTeam(team, winnerTeam));
        if (!winnerParticipates) {
            throw new IllegalArgumentException("Winner team must participate in the hackathon.");
        }

        List<Submission> submissions = teamParticipations.stream()
                .flatMap(participation -> {
                    List<Submission> participationSubmissions = participation.getSubmissions();
                    if (participationSubmissions == null) {
                        return Collections.<Submission>emptyList().stream();
                    }
                    return participationSubmissions.stream();
                })
                .toList();
        if (submissions.isEmpty()) {
            throw new IllegalStateException("Cannot assign winner: no submissions found for this hackathon.");
        }
        boolean allEvaluated = submissions.stream().allMatch(Submission::hasEvaluation);
        if (!allEvaluated) {
            throw new IllegalStateException("Cannot assign winner: not all submissions are evaluated.");
        }

        this.winner = winnerTeam;
    }

    /**
     * Kept for backward compatibility.
     */
    public void proclaimWinner(Team winnerTeam) {
        assignWinner(winnerTeam);
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

    private void ensureStaffCollectionInitialized() {
        if (staff == null) {
            staff = new ArrayList<>();
        }
    }

    private static boolean isSameTeam(Team teamA, Team teamB) {
        return teamA != null
                && teamB != null
                && teamA.getId() != null
                && Objects.equals(teamA.getId(), teamB.getId());
    }

    private static boolean sameStaffMember(StaffAssignment first, StaffAssignment second) {
        if (first == null || second == null || first.getStaffMember() == null || second.getStaffMember() == null) {
            return false;
        }

        UUID firstId = first.getStaffMember().getId();
        UUID secondId = second.getStaffMember().getId();
        return firstId != null && Objects.equals(firstId, secondId);
    }

    private static boolean isValidTransition(HackathonState from, HackathonState to) {
        if (from == null) {
            return to == HackathonState.REGISTRATION;
        }
        return switch (from) {
            case REGISTRATION -> to == HackathonState.ONGOING;
            case ONGOING -> to == HackathonState.EVALUATION;
            case EVALUATION -> to == HackathonState.ENDED;
            case ENDED -> false;
        };
    }

    private static HackathonState nextState(HackathonState currentState) {
        if (currentState == null) {
            throw new IllegalStateException("Hackathon state is not initialized.");
        }
        return switch (currentState) {
            case REGISTRATION -> HackathonState.ONGOING;
            case ONGOING -> HackathonState.EVALUATION;
            case EVALUATION -> HackathonState.ENDED;
            case ENDED -> throw new IllegalStateException("Hackathon already ended.");
        };
    }
}

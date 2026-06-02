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

package org.da_scegliere.progetto_ids_hackathon.core.entities.support;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.da_scegliere.progetto_ids_hackathon.core.entities.staff.StaffAssignment;
import org.da_scegliere.progetto_ids_hackathon.core.entities.staff.StaffMember;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Team;
import org.da_scegliere.progetto_ids_hackathon.core.entities.user.User;
import org.da_scegliere.progetto_ids_hackathon.core.enums.state.support.SupportRequestState;
import org.da_scegliere.progetto_ids_hackathon.core.events.support.SupportRequestAcceptedEvent;
import org.da_scegliere.progetto_ids_hackathon.core.events.support.SupportRequestCreatedEvent;
import org.da_scegliere.progetto_ids_hackathon.core.events.support.SupportRequestRejectedEvent;
import org.da_scegliere.progetto_ids_hackathon.core.policies.BusinessPolicy;
import org.da_scegliere.progetto_ids_hackathon.core.policies.support.SupportRequestMentorSelectionContext;
import org.da_scegliere.progetto_ids_hackathon.core.state.support.SupportRequestLifecycleStateMachine;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Getter
@Entity
public class SupportRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Requested calendar slot date for the mentor-team call.
     */
    @NotNull
    @FutureOrPresent
    private LocalDate dateSlot;

    @NotNull
    @Setter
    @Enumerated(EnumType.STRING)
    private SupportRequestState state;

    @ManyToMany
    @JoinTable(
            name = "support_request_mentors",
            joinColumns = @JoinColumn(name = "support_request_id"),
            inverseJoinColumns = @JoinColumn(name = "staff_assignment_id")
    )
    private List<StaffAssignment> selectedMentors;

    @Setter
    @ManyToOne
    @JoinColumn(name = "accepting_mentor_id")
    private StaffAssignment acceptingMentor;

    @Setter
    @NotNull
    @ManyToOne
    @JoinColumn(name = "sending_team_id", nullable = false)
    private Team sendingTeam;

    public SupportRequest(LocalDate dateSlot, Team sendingTeam, List<StaffAssignment> selectedMentors) {
        this.dateSlot = dateSlot;
        this.sendingTeam = sendingTeam;
        this.selectedMentors = selectedMentors;
        this.state = SupportRequestState.OPEN;
    }

    protected SupportRequest() { }

    /**
     * Explicit semantic accessor for calendar use cases.
     */
    public LocalDate getRequestedCallDate() {
        return dateSlot;
    }

    public void acceptedBy(StaffAssignment acceptingMentor) {
        this.acceptingMentor = Objects.requireNonNull(acceptingMentor, "acceptingMentor must not be null.");
    }

    public void transitionTo(SupportRequestState targetState, SupportRequestLifecycleStateMachine stateMachine) {
        Objects.requireNonNull(targetState, "targetState must not be null.");
        Objects.requireNonNull(stateMachine, "stateMachine must not be null.");

        if (this.state == targetState) {
            return;
        }
        this.state = stateMachine.transition(this.state, targetState);
    }

    public SupportRequestCreatedEvent toCreatedEvent() {
        List<StaffMember> recipients = selectedMentors == null
                ? List.of()
                : selectedMentors.stream()
                .map(StaffAssignment::getStaffMember)
                .filter(Objects::nonNull)
                .toList();
        String sendingTeamName = sendingTeam != null ? sendingTeam.getName() : "Team sconosciuto";
        return new SupportRequestCreatedEvent(sendingTeamName, dateSlot, recipients);
    }

    public SupportRequestAcceptedEvent toAcceptedEvent() {
        String mentorName = acceptingMentor != null && acceptingMentor.getStaffMember() != null
                ? acceptingMentor.getStaffMember().getName()
                : "Mentore assegnato";
        return new SupportRequestAcceptedEvent(mentorName, teamMembersSnapshot());
    }

    public SupportRequestRejectedEvent toRejectedEvent() {
        return new SupportRequestRejectedEvent(teamMembersSnapshot());
    }

    /**
     * Validates mentor selection against team hackathon enrollment context.
     *
     * @param selectedMentors mentors selected in support request.
     * @param teamHackathonIds hackathon ids where sending team participates.
     */
    public static void validateMentorSelection(
            List<StaffAssignment> selectedMentors,
            Set<UUID> teamHackathonIds,
            BusinessPolicy<SupportRequestMentorSelectionContext> mentorSelectionPolicy
    ) {
        Objects.requireNonNull(mentorSelectionPolicy, "mentorSelectionPolicy must not be null.");
        SupportRequestMentorSelectionContext context = new SupportRequestMentorSelectionContext(
                selectedMentors,
                teamHackathonIds
        );
        mentorSelectionPolicy.validate(context);
    }

    private List<User> teamMembersSnapshot() {
        if (sendingTeam == null || sendingTeam.getMembers() == null) {
            return List.of();
        }
        return List.copyOf(sendingTeam.getMembers());
    }

}

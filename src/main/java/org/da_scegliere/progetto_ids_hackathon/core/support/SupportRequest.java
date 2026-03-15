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

package org.da_scegliere.progetto_ids_hackathon.core.support;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.da_scegliere.progetto_ids_hackathon.core.entities.staff.StaffAssignment;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Team;
import org.da_scegliere.progetto_ids_hackathon.core.enums.states.support.SupportRequestState;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Getter
@Entity
public class SupportRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

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
    @OneToOne
    @JoinColumn(name = "sending_team_id")
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

    public void transitionTo(SupportRequestState targetState) {
        Objects.requireNonNull(targetState, "targetState must not be null.");

        if (this.state == targetState) {
            return;
        }
        boolean valid = switch (this.state) {
            case OPEN -> targetState == SupportRequestState.IN_PROGRESS;
            case IN_PROGRESS -> targetState == SupportRequestState.RESOLVED || targetState == SupportRequestState.REJECTED;
            case RESOLVED, REJECTED -> false;
        };

        if (!valid) {
            throw new IllegalStateException("Invalid support request state transition from '" + this.state + "' to '" + targetState + "'.");
        }
        this.state = targetState;
    }

}

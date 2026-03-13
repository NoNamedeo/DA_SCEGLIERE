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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import lombok.Setter;
import org.da_scegliere.progetto_ids_hackathon.core.entities.staff.StaffAssignment;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Team;
import org.da_scegliere.progetto_ids_hackathon.core.states.support.SupportRequestState;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Getter
@Entity
public class SupportRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @PastOrPresent
    private LocalDate date;

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
    @NotNull
    @ManyToOne
    @JoinColumn(name = "accepting_mentor_id")
    private StaffAssignment acceptingMentor;

    @Setter
    @NotNull
    @OneToOne
    @JoinColumn(name = "sending_team_id")
    private Team sendingTeam;

    public SupportRequest(LocalDate date, Team sendingTeam, List<StaffAssignment> selectedMentors) {
        this.date = date;
        this.sendingTeam = sendingTeam;
        this.selectedMentors = selectedMentors;
    }

    protected SupportRequest() { }

    public void acceptedBy(StaffAssignment acceptingMentor) {
        this.acceptingMentor = acceptingMentor;
    }

}

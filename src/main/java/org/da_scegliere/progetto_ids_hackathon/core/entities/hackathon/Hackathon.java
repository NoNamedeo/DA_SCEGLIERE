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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.da_scegliere.progetto_ids_hackathon.core.entities.Participation;
import org.da_scegliere.progetto_ids_hackathon.core.entities.staff.StaffAssignment;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Team;
import org.da_scegliere.progetto_ids_hackathon.core.states.hackathon.HackathonState;

import java.util.List;
import java.util.Objects;
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

    @NotEmpty
    @OneToMany(mappedBy = "hackathon")
    @Setter
    private List<StaffAssignment> staff;

    public Hackathon(String name, String description, List<Participation> participations, List<StaffAssignment> staff) {
        this.name = name;
        this.description = description;
        this.participations = participations;
        this.staff = staff;
        this.hackathonState = HackathonState.REGISTRATION;
        this.winner = null;
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

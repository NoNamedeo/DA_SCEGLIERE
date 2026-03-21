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

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.da_scegliere.progetto_ids_hackathon.core.entities.user.User;
import org.da_scegliere.progetto_ids_hackathon.core.policies.BusinessPolicy;
import org.da_scegliere.progetto_ids_hackathon.core.policies.team.LeaveTeamContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@Entity
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotEmpty
    @Setter
    private String name;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<User> members;

    public Team(String name, List<User> members) {
        this.name = name;
        this.members = members;
    }

    public Team() {}

    public void addMember(User user) {
        Objects.requireNonNull(user, "user must not be null.");
        ensureMembersInitialized();

        if (containsMember(user)) {
            throw new IllegalArgumentException("User is already a member of this team.");
        }

        Team currentTeam = user.getTeam();
        if (currentTeam != null && !isSameTeam(currentTeam)) {
            throw new IllegalArgumentException("User already belongs to another team.");
        }

        members.add(user);
        user.setTeam(this);
    }

    public void removeMember(User user, BusinessPolicy<LeaveTeamContext> leaveTeamPolicy) {
        Objects.requireNonNull(user, "user must not be null.");
        Objects.requireNonNull(leaveTeamPolicy, "leaveTeamPolicy must not be null.");
        ensureMembersInitialized();

        if (!containsMember(user)) {
            throw new IllegalArgumentException("User is not a member of this team.");
        }

        LeaveTeamContext context = new LeaveTeamContext(this);
        leaveTeamPolicy.validate(context);
        members.removeIf(member -> sameUser(member, user));
        user.setTeam(null);
    }

    private void ensureMembersInitialized() {
        if (members == null) {
            members = new ArrayList<>();
        }
    }

    private boolean containsMember(User user) {
        return members.stream().anyMatch(member -> sameUser(member, user));
    }

    private static boolean sameUser(User first, User second) {
        if (first == null || second == null) {
            return false;
        }

        UUID firstId = first.getId();
        UUID secondId = second.getId();
        if (firstId != null && secondId != null) {
            return Objects.equals(firstId, secondId);
        }
        return first == second;
    }

    private boolean isSameTeam(Team otherTeam) {
        if (otherTeam == null) {
            return false;
        }

        if (this.id != null && otherTeam.getId() != null) {
            return Objects.equals(this.id, otherTeam.getId());
        }
        return this == otherTeam;
    }
}

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

package org.da_scegliere.progetto_ids_hackathon.core.entities.team;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import org.da_scegliere.progetto_ids_hackathon.core.entities.user.User;
import org.da_scegliere.progetto_ids_hackathon.core.events.team.TeamCreatedEvent;
import org.da_scegliere.progetto_ids_hackathon.core.events.team.TeamDeletedAfterLeaveEvent;
import org.da_scegliere.progetto_ids_hackathon.core.events.team.TeamDeletedEvent;
import org.da_scegliere.progetto_ids_hackathon.core.events.team.TeamMemberAddedEvent;
import org.da_scegliere.progetto_ids_hackathon.core.events.team.TeamMemberRemovedEvent;
import org.da_scegliere.progetto_ids_hackathon.core.events.payment.WinnerPrizePaidEvent;
import org.da_scegliere.progetto_ids_hackathon.core.exceptions.team.DuplicateTeamMemberException;
import org.da_scegliere.progetto_ids_hackathon.core.exceptions.team.NullTeamMemberException;
import org.da_scegliere.progetto_ids_hackathon.core.exceptions.team.TeamMembersEmptyException;
import org.da_scegliere.progetto_ids_hackathon.core.exceptions.team.TeamNameBlankException;
import org.da_scegliere.progetto_ids_hackathon.core.exceptions.team.UserAlreadyAssignedToAnotherTeamException;
import org.da_scegliere.progetto_ids_hackathon.core.exceptions.team.UserAlreadyInTeamException;
import org.da_scegliere.progetto_ids_hackathon.core.exceptions.team.UserNotInTeamException;
import org.da_scegliere.progetto_ids_hackathon.core.policies.BusinessPolicy;
import org.da_scegliere.progetto_ids_hackathon.core.policies.team.LeaveTeamContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Getter
@Entity
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotEmpty
    private String name;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<User> members;

    public Team(String name, List<User> members) {
        this.members = members == null ? new ArrayList<>() : members;
        rename(name);
    }

    public Team() {
    }

    public static Team create(String name, List<User> initialMembers) {
        if (initialMembers == null || initialMembers.isEmpty()) {
            throw new TeamMembersEmptyException();
        }

        Team team = new Team(name, new ArrayList<>());
        Set<UUID> seenMemberIds = new HashSet<>();
        for (User member : initialMembers) {
            if (member == null) {
                throw new NullTeamMemberException();
            }

            UUID memberId = member.getId();
            if (memberId != null && !seenMemberIds.add(memberId)) {
                throw new DuplicateTeamMemberException(memberId);
            }

            team.addMember(member);
        }
        return team;
    }

    public TeamCreatedEvent toCreatedEvent() {
        ensureMembersInitialized();
        return new TeamCreatedEvent(id, name, List.copyOf(members));
    }

    public void rename(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new TeamNameBlankException();
        }
        this.name = newName;
    }

    public TeamMemberAddedEvent addMember(User user) {
        Objects.requireNonNull(user, "user must not be null.");
        ensureMembersInitialized();

        if (containsMember(user)) {
            throw new UserAlreadyInTeamException(user.getId());
        }

        Team currentTeam = user.getTeam();
        if (currentTeam != null && !isSameTeam(currentTeam)) {
            throw new UserAlreadyAssignedToAnotherTeamException(user.getId());
        }

        members.add(user);
        user.setTeam(this);

        return new TeamMemberAddedEvent(id, name, user, List.copyOf(members));
    }

    public TeamMemberRemovedEvent removeMember(User user, BusinessPolicy<LeaveTeamContext> leaveTeamPolicy) {
        Objects.requireNonNull(user, "user must not be null.");
        Objects.requireNonNull(leaveTeamPolicy, "leaveTeamPolicy must not be null.");
        ensureMembersInitialized();

        if (!containsMember(user)) {
            throw new UserNotInTeamException(user.getId());
        }

        LeaveTeamContext context = new LeaveTeamContext(this);
        leaveTeamPolicy.validate(context);

        members.remove(user);
        user.setTeam(null);
        return new TeamMemberRemovedEvent(id, name, user, List.copyOf(members));
    }

    public TeamDeletedEvent createDeletedEventAndDetachMembers() {
        List<User> formerMembers = detachAllMembers();
        return new TeamDeletedEvent(id, name, formerMembers);
    }

    public TeamDeletedAfterLeaveEvent createDeletedAfterLeaveEventAndDetachMembers(User removedUser) {
        Objects.requireNonNull(removedUser, "removedUser must not be null.");
        List<User> formerMembers = detachAllMembers();
        List<User> membersToNotify = formerMembers.stream()
                .filter(member -> !member.equals(removedUser))
                .toList();
        return new TeamDeletedAfterLeaveEvent(id, name, removedUser, membersToNotify);
    }

    public WinnerPrizePaidEvent toWinnerPrizePaidEvent() {
        ensureMembersInitialized();
        return new WinnerPrizePaidEvent(List.copyOf(members));
    }

    private List<User> detachAllMembers() {
        ensureMembersInitialized();
        List<User> previousMembers = List.copyOf(members);
        for (User member : previousMembers) {
            member.setTeam(null);
        }
        members.clear();
        return previousMembers;
    }

    public boolean hasMember(User user) {
        ensureMembersInitialized();
        return containsMember(user);
    }

    private void ensureMembersInitialized() {
        if (members == null) {
            members = new ArrayList<>();
        }
    }

    private boolean containsMember(User user) {
        return members.contains(user);
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

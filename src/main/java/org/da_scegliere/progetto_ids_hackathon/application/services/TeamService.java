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
import org.da_scegliere.progetto_ids_hackathon.application.ports.events.DomainEventPublisher;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.ITeamRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IUserRepository;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.team.TeamNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.user.UserNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Team;
import org.da_scegliere.progetto_ids_hackathon.core.entities.user.User;
import org.da_scegliere.progetto_ids_hackathon.core.events.team.TeamDeletedAfterLeaveEvent;
import org.da_scegliere.progetto_ids_hackathon.core.events.team.TeamDeletedEvent;
import org.da_scegliere.progetto_ids_hackathon.core.events.team.TeamMemberAddedEvent;
import org.da_scegliere.progetto_ids_hackathon.core.events.team.TeamMemberRemovedEvent;
import org.da_scegliere.progetto_ids_hackathon.core.exceptions.team.TeamMinimumMembersViolationException;
import org.da_scegliere.progetto_ids_hackathon.core.exceptions.team.TeamNameBlankException;
import org.da_scegliere.progetto_ids_hackathon.core.exceptions.team.UserNotInTeamException;
import org.da_scegliere.progetto_ids_hackathon.core.exceptions.team.UserWithoutTeamException;
import org.da_scegliere.progetto_ids_hackathon.core.policies.BusinessPolicy;
import org.da_scegliere.progetto_ids_hackathon.core.policies.team.LeaveTeamContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service for team management use cases.
 * <p>
 * Orchestration only: business invariants live inside domain entities and policies.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TeamService {

    private final ITeamRepository teamRepository;
    private final IUserRepository userRepository;
    private final BusinessPolicy<LeaveTeamContext> leaveTeamPolicy;
    private final DomainEventPublisher domainEventPublisher;

    public List<Team> getTeams() {
        return List.copyOf(teamRepository.findAll());
    }

    public Team getTeamById(UUID teamId) {
        requireNonNullId(teamId, "teamId");
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(teamId));
    }

    public Team getTeamByName(String name) {
        if (name == null || name.isBlank()) {
            throw new TeamNameBlankException();
        }
        return teamRepository.findTeamByName(name)
                .orElseThrow(() -> new TeamNotFoundException(name));
    }

    public Team getTeamByTeamMemberId(UUID teamMemberId) {
        requireNonNullId(teamMemberId, "teamMemberId");
        return teamRepository.findByMembers_id(teamMemberId)
                .orElseThrow(() -> new TeamNotFoundException(teamMemberId));
    }

    @Transactional
    public Team createTeam(String name, List<User> members) {
        log.info("Creating team name={} initialMembersCount={}", name, members == null ? 0 : members.size());

        Team team = Team.create(name, members);
        Team savedTeam = teamRepository.save(team);

        domainEventPublisher.publish(savedTeam.toCreatedEvent());

        log.info("Created team teamId={} name={}", savedTeam.getId(), savedTeam.getName());
        return savedTeam;
    }

    @Transactional
    public void deleteTeam(UUID teamId) {
        log.info("Deleting team teamId={}", teamId);

        Team team = getTeamById(teamId);
        TeamDeletedEvent event = team.createDeletedEventAndDetachMembers();

        teamRepository.delete(team);
        domainEventPublisher.publish(event);

        log.info("Deleted team teamId={}", teamId);
    }

    @Transactional
    public Team changeTeamName(UUID teamId, String newName) {
        log.info("Changing team name teamId={} newName={}", teamId, newName);

        Team team = getTeamById(teamId);
        team.rename(newName);

        log.info("Changed team name teamId={} newName={}", teamId, newName);
        return team;
    }

    @Transactional
    public Team addMemberToTeam(UUID teamId, UUID userId) {
        log.info("Adding team member userId={} to teamId={}", userId, teamId);

        Team team = getTeamById(teamId);
        User user = getUserById(userId);

        TeamMemberAddedEvent event = team.addMember(user);
        domainEventPublisher.publish(event);

        log.info("Added team member userId={} to teamId={}", userId, teamId);
        return team;
    }

    @Transactional
    public Optional<Team> removeMemberFromTeam(UUID teamId, UUID userId) {
        log.info("Removing team member userId={} from teamId={}", userId, teamId);

        Team team = getTeamById(teamId);
        User user = getUserById(userId);

        if (!team.hasMember(user)) {
            throw new UserNotInTeamException(userId);
        }

        try {
            TeamMemberRemovedEvent event = team.removeMember(user, leaveTeamPolicy);
            domainEventPublisher.publish(event);

            log.info("Removed team member userId={} from teamId={}", userId, teamId);
            return Optional.of(team);
        } catch (TeamMinimumMembersViolationException ex) {
            return deleteTeamAfterMemberLeave(team, user);
        }
    }

    @Transactional
    public Optional<Team> leaveCurrentTeam(UUID userId) {
        log.info("Leaving current team userId={}", userId);

        User user = getUserById(userId);
        Team currentTeam = user.getTeam();
        if (currentTeam == null) {
            throw new UserWithoutTeamException(userId);
        }

        return removeMemberFromTeam(currentTeam.getId(), userId);
    }

    private Optional<Team> deleteTeamAfterMemberLeave(Team team, User removedUser) {
        TeamDeletedAfterLeaveEvent event = team.createDeletedAfterLeaveEventAndDetachMembers(removedUser);
        teamRepository.delete(team);
        domainEventPublisher.publish(event);

        log.info("Removed team member userId={} and deleted teamId={}", removedUser.getId(), team.getId());
        return Optional.empty();
    }

    private User getUserById(UUID userId) {
        requireNonNullId(userId, "userId");
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private static void requireNonNullId(UUID id, String fieldName) {
        if (id == null) {
            throw new IllegalArgumentException(fieldName + " must not be null.");
        }
    }
}

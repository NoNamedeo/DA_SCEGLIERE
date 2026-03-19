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
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.ITeamRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IUserRepository;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.moderation.UserNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.team.TeamNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Team;
import org.da_scegliere.progetto_ids_hackathon.core.entities.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Application service for team management use cases.
 * <p>
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TeamService{

    private final ITeamRepository teamRepository;
    private final IUserRepository userRepository;

    /**
     * Returns all teams currently stored.
     *
     * @return immutable snapshot of all teams.
     */
    public List<Team> getTeams() {
        return List.copyOf(teamRepository.findAll());
    }

    /**
     * Returns one team by id.
     *
     * @param teamId team identifier.
     * @return the requested team.
     * @throws IllegalArgumentException if {@code teamId} is {@code null}.
     * @throws TeamNotFoundException if no team exists for the provided id.
     */
    public Team getTeamById(UUID teamId) {
        log.info("Get team teamId={}", teamId);

        if(teamId == null) {
            throw new IllegalArgumentException("teamId must not be null");
        }
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(teamId));
    }

    /**
     * Returns one team by the provided name.
     *
     * @param name team name.
     * @return the requested team.
     * @throws IllegalArgumentException if {@code name} is blank.
     * @throws TeamNotFoundException if no team exists for the provided name.
     */
    public Team getTeamByName(String name) {
        log.info("Get team by name name={}", name);

        if(name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        return teamRepository.findTeamByName(name)
                .orElseThrow(() -> new TeamNotFoundException(name));
    }

    /**
     * Returns one team by the provided team member id.
     *
     * @param teamMemberId member identifier.
     * @return the team where the member belongs.
     * @throws IllegalArgumentException if {@code teamMemberId} is {@code null}.
     * @throws TeamNotFoundException if no team exists for the provided id.
     */
    public Team getTeamByTeamMemberId(UUID teamMemberId) {
        log.info("Get team by teamMemberId={}", teamMemberId);

        if(teamMemberId == null) {
            throw new IllegalArgumentException("teamMemberId must not be null");
        }
        return teamRepository.findByMembers_id(teamMemberId)
                .orElseThrow(() -> new TeamNotFoundException(teamMemberId));
    }

    /**
     * Creates and persists a new team with the provided parameters.
     *
     * @param name team name.
     * @param members initial team members.
     * @return persisted team aggregate.
     */
    @Transactional
    public Team createTeam(String name, List<User> members) {
        log.info("Create team name={} members={}", name, members);

        return teamRepository.save(new Team(name, members));
    }

    /**
     * Deletes a team by id.
     *
     * @param teamId team identifier.
     * @throws TeamNotFoundException if the team does not exist.
     */
    @Transactional
    public void deleteTeam(UUID teamId) {
        log.info("Delete team teamId={}", teamId);

        Team team = getTeamById(teamId);
        teamRepository.delete(team);

        log.info("Deleted team teamId={}", teamId);
    }

    /**
     * Updates team name.
     *
     * @param teamId team identifier.
     * @param newName new team name.
     * @return updated team aggregate.
     * @throws IllegalArgumentException if {@code teamId} is {@code null}.
     * @throws TeamNotFoundException if the team does not exist.
     */
    @Transactional
    public Team changeTeamName(UUID teamId, String newName) {
        log.info("Change team name teamId={}, newName={}", teamId, newName);

        if(teamId == null) {
            throw new IllegalArgumentException("teamId must not be null");
        }
        Team team = getTeamById(teamId);
        team.setName(newName);

        log.info("Changed team name teamId={}, newName={}", teamId, newName);
        return team;
    }

    /**
     * Adds a user to a team.
     *
     * @param teamId team identifier.
     * @param userId user to add.
     * @return updated team aggregate.
     * @throws TeamNotFoundException if the team does not exist.
     */
    @Transactional
    public Team addMemberToTeam(UUID teamId, UUID userId) {
        log.info("Add team member to team teamId={}, userId={}", teamId, userId);

        Team team = getTeamById(teamId);
        User user = getUserById(userId);
        team.addMember(user);

        log.info("Added team member to team teamId={}, userId={}", teamId, userId);
        return team;
    }

    /**
     * Removes a user from a team resolving entities by id.
     *
     * @param teamId team identifier.
     * @param userId user identifier.
     * @return updated team aggregate.
     */
    @Transactional
    public Team removeMemberFromTeam(UUID teamId, UUID userId) {
        log.info("Remove team member from team teamId={}, userId={}", teamId, userId);

        Team team = getTeamById(teamId);
        User user = getUserById(userId);
        validateMembership(team, userId);
        team.removeMember(user);

        log.info("Removed team member from team teamId={}, userId={}", teamId, userId);
        return team;
    }

    private static void validateMembership(Team team, UUID userId) {
        boolean isMember = team.getMembers() != null
                && team.getMembers().stream()
                .anyMatch(member -> member != null && userId.equals(member.getId()));
        if (!isMember) {
            throw new IllegalArgumentException("User is not a member of the specified team.");
        }
    }

    private User getUserById( UUID userId ) {
        if(userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }
}

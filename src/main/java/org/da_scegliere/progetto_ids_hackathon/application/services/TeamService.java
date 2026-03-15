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

import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.ITeamRepository;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.team.TeamNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Team;
import org.da_scegliere.progetto_ids_hackathon.core.entities.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Application service for team management use cases.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Read teams by different lookup keys (id, name, member id).</li>
 *     <li>Create, update and delete team aggregates.</li>
 *     <li>Orchestrate team membership changes through domain methods.</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class TeamService{

    private final ITeamRepository teamRepository;

    /**
     * Creates a new service instance.
     *
     * @param teamRepository repository for team persistence and lookup operations.
     * @throws NullPointerException when {@code teamRepository} is {@code null}.
     */
    @Autowired
    public TeamService(ITeamRepository teamRepository) {
        this.teamRepository = Objects.requireNonNull(teamRepository, "teamRepository must not be null.");
    }

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
        Team team = getTeamById(teamId);
        teamRepository.delete(team);
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
        if(teamId == null) {
            throw new IllegalArgumentException("teamId must not be null");
        }
        Team team = getTeamById(teamId);
        team.setName(newName);
        return team;
    }

    /**
     * Adds a user to a team.
     *
     * @param teamId team identifier.
     * @param user user to add.
     * @return updated team aggregate.
     * @throws TeamNotFoundException if the team does not exist.
     */
    @Transactional
    public Team addMemberToTeam(UUID teamId, User user) {
        Team team = getTeamById(teamId);
        team.addMember(user);
        return team;
    }

    /**
     * Removes a user from a team.
     *
     * @param teamId team identifier.
     * @param user user to remove.
     * @return updated team aggregate.
     * @throws TeamNotFoundException if the team does not exist.
     */
    @Transactional
    public Team removeMemberFromTeam(UUID teamId, User user) {
        Team team = getTeamById(teamId);
        team.removeMember(user);
        return team;
    }
}

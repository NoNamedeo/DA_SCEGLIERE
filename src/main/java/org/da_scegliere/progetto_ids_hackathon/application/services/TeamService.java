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

package org.da_scegliere.progetto_ids_hackathon.Application.services;

public class TeamService{

    private ITeamRepository teamRepository;

    @Autowired
    public TeamService(ITeamRepository teamRepository) {
        this.teamRepository = Objects.requireNonNull(teamRepository, "teamRepository must not be null.");
    }

    /**
     * Returns all teams currently stored.
     */
    public List<Team> getTeams() {
        return List.copyOf(teamRepository.findAll());
    }

    /**
     * Returns one team by id.
     *
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
     * @throws TeamNotFoundException if no team exists for the provided name.
     */
    public Team getTeamByName(String name) {
        if(name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        return teamRepository.findByName(name)
                .orElseThrow(() -> new TeamNotFoundException(name));
    }

    /**
     * Returns one team by the provided team member id.
     *
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
     */
    @Transactional
    public Team createTeam(String name, List<User> members) {
        Team team = new Team(name, members);
        return teamRepository.save(team);
    }

    /**
     * Updates team name.
     */
    @Transactional
    public Team changeTeamName(UUID teamId, String newName) {
        if(teamId == null) {
            throw new IllegalArgumentException("teamId must not be null");
        }
        Team team = getTeamById(teamId);
        team.setName(newName);
        return teamRepository.save(team);
    }

    /**
     * add a team member to the team.
     */
    @Transactional
    public Team addMemberToTeam(UUID teamId, User user) {
        Team team = getTeamById(teamId);
        team.addMember(user);
        return teamRepository.save(team);
    }

    /**
     * remove a team member from the team.
     */
    @Transactional
    public Team removeMemberFromTeam(UUID teamId, User user) {
        Team team = getTeamById(teamId);
        team.removeMember(user);
        return teamRepository.save(team);
    }
}

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
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.INotificationRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.ITeamRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IUserRepository;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.user.UserNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.team.TeamNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.core.entities.notification.BaseNotification;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Team;
import org.da_scegliere.progetto_ids_hackathon.core.entities.user.User;
import org.da_scegliere.progetto_ids_hackathon.core.policies.BusinessPolicy;
import org.da_scegliere.progetto_ids_hackathon.core.policies.team.LeaveTeamContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
    private final BusinessPolicy<LeaveTeamContext> leaveTeamPolicy;
    private final INotificationRepository notificationRepository;

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
        log.debug("Retrieving team by teamId={}.", teamId);

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
        log.debug("Retrieving team by name={}.", name);

        if (name == null || name.isBlank()) {
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
        log.debug("Retrieving team by teamMemberId={}.", teamMemberId);

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
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank.");
        }
        if (members == null || members.isEmpty()) {
            throw new IllegalArgumentException("members must not be null or empty.");
        }
        if (members.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("members must not contain null values.");
        }
        ensureDistinctMembers(members);
        ensureAllUsersWithoutTeam(members);

        log.info("Creating team name={} initialMembersCount={}.", name, members.size());

        Team team = new Team(name, new ArrayList<>());
        members.forEach(team::addMember);

        for(User user : members) {
            BaseNotification notification = new BaseNotification("Team creato", "il team è stato creato", user, 3);
            notificationRepository.save(notification);
        }

        Team savedTeam = teamRepository.save(team);
        log.info("Created team teamId={} name={}.", savedTeam.getId(), savedTeam.getName());
        return savedTeam;
    }

    /**
     * Deletes a team by id.
     *
     * @param teamId team identifier.
     * @throws TeamNotFoundException if the team does not exist.
     */
    @Transactional
    public void deleteTeam(UUID teamId) {
        log.info("Deleting team teamId={}.", teamId);

        Team team = getTeamById(teamId);

        for(User user : team.getMembers()){
            user.setTeam(null);
            BaseNotification notification = new BaseNotification(
                    "Team cancellato",
                    "il team: " + team.getName() + " è stato cancellato",
                    user, 3);
            notificationRepository.save(notification);
        }

        teamRepository.delete(team);

        log.info("Deleted team teamId={}.", teamId);
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
        log.info("Changing team name teamId={} newName={}.", teamId, newName);

        if(teamId == null) {
            throw new IllegalArgumentException("teamId must not be null");
        }
        Team team = getTeamById(teamId);
        team.setName(newName);

        log.info("Changed team name teamId={} newName={}.", teamId, newName);
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
        log.info("Adding team member userId={} to teamId={}.", userId, teamId);
        requireNonNullId(teamId, "teamId");
        requireNonNullId(userId, "userId");

        Team team = getTeamById(teamId);
        User user = getUserById(userId);
        ensureUserCanJoinTeam(user, team);
        team.addMember(user);

        for(User userToNotify : team.getMembers()){
            BaseNotification notification = new BaseNotification("Nuovo membro", "il team ha un nuovo membro: "+user.getName(), userToNotify, 3);
            notificationRepository.save(notification);
        }

        log.info("Added team member userId={} to teamId={}.", userId, teamId);
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
    public Optional<Team> removeMemberFromTeam(UUID teamId, UUID userId) {
        log.info("Removing team member userId={} from teamId={}.", userId, teamId);
        requireNonNullId(teamId, "teamId");
        requireNonNullId(userId, "userId");

        Team team = getTeamById(teamId);
        User user = getUserById(userId);
        validateMembership(team, user);

        try{
            team.removeMember(user, leaveTeamPolicy);
            for (User remainingMember : team.getMembers()) {
                BaseNotification notification = new BaseNotification(
                        "Membro uscito dal team",
                        "Il membro " + user.getName() + " ha abbandonato il team " + team.getName() + ".",
                        remainingMember,
                        3
                );
                notificationRepository.save(notification);
            }
            log.info("Removed team member userId={} from teamId={}.", userId, teamId);
            return Optional.of(team);
        }
        catch(IllegalStateException ex){
            List<User> membersBeforeDelete = List.copyOf(team.getMembers());

            for (User member : membersBeforeDelete) {
                member.setTeam(null);
            }

            membersBeforeDelete.stream()
                    .filter(member -> !Objects.equals(member.getId(), userId))
                    .forEach(member -> notificationRepository.save(
                            new BaseNotification(
                                    "Team cancellato",
                                    "Il team " + team.getName() + " è stato cancellato dopo l'abbandono di " + user.getName() + ".",
                                    member,
                                    3
                            )
                    ));

            teamRepository.delete(team);
            log.info("Removed team member userId={} and deleted now-empty teamId={}.", userId, teamId);
            return Optional.empty();
        }
    }

    /**
     * Removes a user from their current team.
     *
     * @param userId user identifier.
     * @return optional updated team; empty when the team is deleted as a consequence of leave.
     */
    @Transactional
    public Optional<Team> leaveCurrentTeam(UUID userId) {
        log.info("Leaving current team userId={}.", userId);
        requireNonNullId(userId, "userId");

        User user = getUserById(userId);
        Team currentTeam = user.getTeam();
        if (currentTeam == null) {
            throw new IllegalArgumentException("User does not belong to any team.");
        }

        return removeMemberFromTeam(currentTeam.getId(), userId);
    }

    private static void validateMembership(Team team, User user) {
        Team currentUserTeam = user.getTeam();
        UUID teamId = team.getId();
        UUID currentUserTeamId = currentUserTeam != null ? currentUserTeam.getId() : null;

        boolean userPointsToTeam = currentUserTeam != null && Objects.equals(currentUserTeamId, teamId);
        boolean isMember = team.getMembers() != null
                && team.getMembers().stream()
                .anyMatch(member -> member != null && Objects.equals(user.getId(), member.getId()));

        if (!userPointsToTeam || !isMember) {
            throw new IllegalArgumentException("User is not a member of the specified team.");
        }
    }

    private static void ensureDistinctMembers(List<User> members) {
        Set<UUID> seenIds = new HashSet<>();
        for (User member : members) {
            UUID memberId = member.getId();
            if (memberId != null && !seenIds.add(memberId)) {
                throw new IllegalArgumentException("members must not contain duplicates.");
            }
        }
    }

    private static void ensureAllUsersWithoutTeam(List<User> members) {
        for (User member : members) {
            if (member.getTeam() != null) {
                throw new IllegalArgumentException("Cannot create team with users already assigned to a team.");
            }
        }
    }

    private static void ensureUserCanJoinTeam(User user, Team targetTeam) {
        Team currentTeam = user.getTeam();
        if (currentTeam == null) {
            return;
        }

        UUID currentTeamId = currentTeam.getId();
        UUID targetTeamId = targetTeam.getId();

        if (Objects.equals(currentTeamId, targetTeamId)) {
            throw new IllegalArgumentException("User is already a member of this team.");
        }
        throw new IllegalArgumentException("User already belongs to another team.");
    }

    private static void requireNonNullId(UUID id, String fieldName) {
        if (id == null) {
            throw new IllegalArgumentException(fieldName + " must not be null.");
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

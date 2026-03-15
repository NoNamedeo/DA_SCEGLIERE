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
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IUserRepository;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.UserNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.hackathon.HackathonNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.team.TeamNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Team;
import org.da_scegliere.progetto_ids_hackathon.core.entities.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Application service for user management and user-to-team lookup operations.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Read users by id, name and team membership context.</li>
 *     <li>Create, update and delete user entities.</li>
 *     <li>Coordinate user-team relationship retrieval through repositories.</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class UserService{

    private IUserRepository userRepository;
    private ITeamRepository teamRepository;

    /**
     * Creates a new service instance.
     *
     * @param userRepository repository for user persistence and lookup operations.
     * @param teamRepository repository used to resolve teams for membership queries.
     */
    @Autowired
    public UserService(IUserRepository userRepository, ITeamRepository teamRepository) {
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
    }

    /**
     * Retrieves all users.
     *
     * @return immutable snapshot of all users.
     */
    public List<User> getAllUsers() {
        return List.copyOf(userRepository.findAll());
    }

    /**
     * Retrieves a user by identifier.
     *
     * @param userId user identifier.
     * @return the requested user.
     * @throws IllegalArgumentException when {@code userId} is {@code null}.
     * @throws UserNotFoundException when no user exists for the provided id.
     */
    public User getUserById(UUID userId) {
        if(userId == null){
            throw new IllegalArgumentException("UserId cannot be null.");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    /**
     * Retrieves a user by name.
     *
     * @param name user name.
     * @return the requested user.
     * @throws IllegalArgumentException when {@code name} is blank.
     */
    public User getUserByName(String name) {
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }

        return userRepository.findUserByName(name)
                .orElseThrow(() -> new HackathonNotFoundException(name));
    }

    /**
     * Retrieves all users belonging to a specific team.
     *
     * @param teamId team identifier.
     * @return immutable list of team members.
     * @throws IllegalArgumentException when {@code teamId} is {@code null}.
     * @throws TeamNotFoundException when the team does not exist.
     */
    public List<User> getUserByTeam(UUID teamId) {
        if(teamId == null){
            throw new IllegalArgumentException("TeamId cannot be null.");
        }
        Team team = teamRepository.findTeamById(teamId);
        if(team == null){
            throw new TeamNotFoundException(teamId);
        }
        return List.copyOf(team.getMembers());
    }

    /**
     * Creates and persists a user.
     *
     * @param name user display name.
     * @param age user age.
     * @param email user e-mail.
     * @param team team currently associated to the user.
     * @return persisted user.
     */
    @Transactional
    public User createUser(String name, int age, String email, Team team){
        User user = new User(name, age, email, team);
        return userRepository.save(user);
    }

    /**
     * Updates the user's name.
     *
     * @param userId user identifier.
     * @param name new user name.
     * @return persisted updated user.
     * @throws UserNotFoundException when user does not exist.
     */
    @Transactional
    public User changeUserName(UUID userId, String name){
        User user = getUserById(userId);
        user.setName(name);
        return userRepository.save(user);
    }

    /**
     * Deletes a user by identifier.
     *
     * @param userId user identifier.
     * @throws UserNotFoundException when user does not exist.
     */
    @Transactional
    public void deleteUser(UUID userId) {
        userRepository.delete(getUserById(userId));
    }
}

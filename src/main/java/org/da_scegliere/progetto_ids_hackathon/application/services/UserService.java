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
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IUserRepository;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.user.UserNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.team.TeamNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Team;
import org.da_scegliere.progetto_ids_hackathon.core.entities.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Application service for user management and user-to-team lookup operations.
 * <p>
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService{

    private final IUserRepository userRepository;
    private final TeamService teamService;

    /**
     * Retrieves all users.
     *
     * @return immutable snapshot of all users.
     */
    public List<User> getAllUsers() {
        log.debug("Retrieving all users.");
        List<User> users = List.copyOf(userRepository.findAll());
        log.debug("Retrieved {} users.", users.size());
        return users;
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
        log.debug("Retrieving user by id={}.", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        log.debug("Retrieved user id={}.", userId);
        return user;
    }

    /**
     * Retrieves a user by email.
     *
     * @param email user email.
     * @return the requested user.
     * @throws IllegalArgumentException when {@code email} is {@code null}.
     * @throws UserNotFoundException when no user exists for the provided email.
     */
    public User getUserByEmail(String email) {
        if(email == null || email.isBlank()){
            throw new IllegalArgumentException("Email must not be blank or null.");
        }
        log.debug("Retrieving user by email={}.", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
        log.debug("Retrieved user by email={}.", email);
        return user;
    }

    /**
     * Retrieves a user by name.
     *
     * @param name user name.
     * @return the requested user.
     * @throws IllegalArgumentException when {@code name} is blank.
     */
    public User getUserByName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank or null.");
        }
        log.debug("Retrieving user by name={}.", name);
        User user = userRepository.findUserByName(name)
                .orElseThrow(() -> new UserNotFoundException(name));
        log.debug("Retrieved user by name={}.", name);
        return user;
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
        log.debug("Retrieving users for teamId={}.", teamId);
        Team team = teamService.getTeamById(teamId);
        if(team == null){
            throw new TeamNotFoundException(teamId);
        }
        List<User> users = List.copyOf(team.getMembers());
        log.debug("Retrieved {} users for teamId={}.", users.size(), teamId);
        return users;
    }

    /**
     * Creates and persists a user.
     *
     * @param name user display name.
     * @param age user age.
     * @param email user e-mail.
     * @param teamId optional team currently associated to the user.
     * @return persisted user.
     */
    @Transactional
    public User createUser(String name, int age, String email, UUID teamId){
        log.info("Creating user for teamId={} email={}.", teamId, email);
        Team team = teamId == null ? null : teamService.getTeamById(teamId);
        User user = new User(name, age, email, team);
        User savedUser = userRepository.save(user);
        log.info("Created user id={} for teamId={}.", savedUser.getId(), teamId);
        return savedUser;
    }

    /**
     * Updates the user's name.
     *
     * @param userId user identifier.
     * @param name new username.
     * @return persisted updated user.
     * @throws UserNotFoundException when user does not exist.
     */
    @Transactional
    public User changeUserName(UUID userId, String name){
        log.info("Changing user name userId={}.", userId);
        User user = getUserById(userId);
        user.setName(name);
        User updatedUser = userRepository.save(user);
        log.info("Changed user name userId={}.", userId);
        return updatedUser;
    }

    /**
     * Deletes a user by identifier.
     *
     * @param userId user identifier.
     * @throws UserNotFoundException when user does not exist.
     */
    @Transactional
    public void deleteUser(UUID userId) {
        log.info("Deleting user userId={}.", userId);
        userRepository.delete(getUserById(userId));
        log.info("Deleted user userId={}.", userId);
    }
}

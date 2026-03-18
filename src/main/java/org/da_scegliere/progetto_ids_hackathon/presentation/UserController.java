
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

package org.da_scegliere.progetto_ids_hackathon.presentation.controllers;

import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.da_scegliere.progetto_ids_hackathon.application.services.UserService;
import org.da_scegliere.progetto_ids_hackathon.core.entities.user.User;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.user.request.CreateUserRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.user.request.UpdateUserRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.user.response.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for user management endpoints.
 * <p>
 * This layer is intentionally thin: it validates and maps HTTP payloads,
 * then delegates use cases to the application service.
 */
@Log4j2
@Validated
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController( UserService userService) {
        this.userService = userService;
    }

    /**
     * Lists users.
     * <p>
     * If {@code teamId} is provided, returns members of that team only.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserResponse>> getUsers(@RequestParam(required = false) UUID teamId) {
        log.info("Received request to list users. teamIdFilterPresent={}", teamId != null);

        List<User> users = teamId == null
                ? userService.getAllUsers()
                : userService.getUsersByTeam(teamId);

        List<UserResponse> response = users.stream()
                .map(UserController::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves one user by identifier.
     */
    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {
        log.info("Received request to retrieve user. userId={}", userId);

        User user = userService.getUserById(userId);
        return ResponseEntity.ok(toResponse(user));
    }

    /**
     * Creates a new user.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("Received request to create user.");

        User createdUser = userService.createUser(
                request.name(),
                request.age(),
                request.email(),
                request.teamId()
        );
        UserResponse response = toResponse(createdUser);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{userId}")
                .buildAndExpand(response.id())
                .toUri();

        log.info("User created successfully. userId={}", response.id());
        return ResponseEntity.created(location).body(response);
    }

    /**
     * Updates user name.
     */
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        log.info("Received request to update user. userId={}", userId);

        User updatedUser = userService.updateUserName(userId, request.name());
        log.info("User updated successfully. userId={}", userId);
        return ResponseEntity.ok(toResponse(updatedUser));
    }

    /**
     * Deletes a user.
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        log.warn("Received request to delete user. userId={}", userId);

        userService.deleteUser(userId);
        log.warn("User deleted successfully. userId={}", userId);
        return ResponseEntity.noContent().build();
    }

    private static UserResponse toResponse(User user) {
        UUID teamId = user.getTeam() != null ? user.getTeam().getId() : null;
        String accountStatus = user.getAccountStatus() != null ? user.getAccountStatus().name() : null;

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getAge(),
                user.getEmail(),
                accountStatus,
                teamId
        );
    }
}

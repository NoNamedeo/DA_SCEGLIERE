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

package org.da_scegliere.progetto_ids_hackathon.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.da_scegliere.progetto_ids_hackathon.application.services.NotificationService;
import org.da_scegliere.progetto_ids_hackathon.application.services.TeamService;
import org.da_scegliere.progetto_ids_hackathon.application.services.UserService;
import org.da_scegliere.progetto_ids_hackathon.core.entities.notification.AbstractNotification;
import org.da_scegliere.progetto_ids_hackathon.core.entities.user.User;
import org.da_scegliere.progetto_ids_hackathon.core.enums.state.user.AccountState;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.user.CreateUserRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.user.UpdateUserRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.notifications.NotificationResponse;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.user.UserResponse;
import org.da_scegliere.progetto_ids_hackathon.presentation.mapper.NotificationMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final TeamService teamService;
    private final NotificationService notificationService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {
        return ResponseEntity.ok(toResponse(userService.getUserById(userId)));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsers(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) UUID teamId,
            @RequestParam(required = false) AccountState status
    ) {
        int activeFilters = (email != null ? 1 : 0) + (name != null ? 1 : 0) + (teamId != null ? 1 : 0);
        if (activeFilters > 1) {
            throw new IllegalArgumentException("Use only one among email, name or teamId.");
        }

        List<User> users;
        if (email != null) {
            users = List.of(userService.getUserByEmail(email));
        } else if (name != null) {
            users = List.of(userService.getUserByName(name));
        } else if (teamId != null) {
            users = userService.getUserByTeam(teamId);
        } else {
            users = userService.getAllUsers();
        }

        if (status != null) {
            users = users.stream()
                    .filter(user -> user.getAccountStatus() == status)
                    .toList();
        }

        return ResponseEntity.ok(users.stream().map(UserController::toResponse).toList());
    }

    @GetMapping("/{userId}/notifications")
    public ResponseEntity<List<NotificationResponse>> getUserNotifications(@PathVariable UUID userId) {
        List<AbstractNotification> notifications = notificationService.getNotificationsByTargetId(userId);

        return ResponseEntity.ok(notifications.stream().map(NotificationMapper::toNotificationResponse).toList());
    }

    @PostMapping
    public ResponseEntity<Void> createUser(@Valid @RequestBody CreateUserRequest request) {
        User createdUser = userService.createUser(
                request.name(),
                request.age(),
                request.email(),
                request.teamId()
        );
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{userId}")
                .buildAndExpand(createdUser.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        User updatedUser = userService.changeUserName(userId, request.name());
        return ResponseEntity.ok(toResponse(updatedUser));
    }

    @DeleteMapping("/{userId}/team")
    public ResponseEntity<Void> leaveCurrentTeam(@PathVariable UUID userId) {
        teamService.leaveCurrentTeam(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    private static UserResponse toResponse(User user) {
        UUID teamId = user.getTeam() != null ? user.getTeam().getId() : null;
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getAge(),
                user.getEmail(),
                user.getAccountStatus(),
                teamId
        );
    }
}

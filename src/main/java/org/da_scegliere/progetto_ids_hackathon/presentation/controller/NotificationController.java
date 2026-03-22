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

package org.da_scegliere.progetto_ids_hackathon.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.da_scegliere.progetto_ids_hackathon.application.services.moderation.manager.ManagerService;
import org.da_scegliere.progetto_ids_hackathon.application.services.NotificationService;
import org.da_scegliere.progetto_ids_hackathon.application.services.StaffService;
import org.da_scegliere.progetto_ids_hackathon.application.services.UserService;
import org.da_scegliere.progetto_ids_hackathon.core.entities.notification.AbstractNotification;
import org.da_scegliere.progetto_ids_hackathon.core.enums.state.notification.NotificationStatus;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.notifications.NotificationResponse;
import org.da_scegliere.progetto_ids_hackathon.presentation.mapper.NotificationMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;
    private final StaffService staffService;
    private final ManagerService managerService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getAllNotifications() {
        List<AbstractNotification> notifications = notificationService.getAllNotifications();
        return ResponseEntity.ok(NotificationMapper.toNotificationResponseList(notifications));
    }

    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationResponse> getNotificationById(
            @PathVariable UUID notificationId
    ){
        AbstractNotification notification = notificationService.getNotificationById(notificationId);
        return ResponseEntity.ok(NotificationMapper.toNotificationResponse(notification));
    }

    @PatchMapping("/{notificationId}")
    public ResponseEntity<NotificationResponse> updateNotification( @RequestParam NotificationStatus notificationStatus, @PathVariable UUID notificationId ){
        notificationService.changeNotificationStatus(notificationId, notificationStatus);

        return ResponseEntity.ok(NotificationMapper.toNotificationResponse(notificationService.getNotificationById(notificationId)));
    }
}

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
import org.da_scegliere.progetto_ids_hackathon.core.entities.notification.AbstractNotification;
import org.da_scegliere.progetto_ids_hackathon.core.entities.notification.Notification;
import org.da_scegliere.progetto_ids_hackathon.core.entities.user.AbstractUser;
import org.da_scegliere.progetto_ids_hackathon.core.enums.state.notification.NotificationStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationService {

    private final INotificationRepository notificationRepository;

    public List<Notification> getAllNotifications() {
        log.info("Getting all notifications");
        return List.copyOf(notificationRepository.findAll());
    }

    public Notification getNotificationById(UUID notificationId) {
        if(notificationId == null) {
            return null;
        }
        log.info("Getting notification by id: {}", notificationId);
        return notificationRepository.findById(notificationId).orElse(null);
    }

    @Transactional
    public Notification createNotification(String title, String message, AbstractUser target, NotificationStatus notificationStatus, int priority) {
        log.info("Creating notification with title={}", title);

        Notification notification = new Notification(title, message, target, priority);

        log.info("Notification created with id: {}", notification.getId());
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification changeNotificationTarget(AbstractUser target, UUID notificationId) {
        if(notificationId == null) {
            throw new IllegalArgumentException("notificationId must not be null.");
        }
        if(target == null) {
            throw new IllegalArgumentException("target must not be null.");
        }
        log.info("Changing notification target with id: {}", notificationId);
        Notification notification = getNotificationById(notificationId);
        notification.setTarget(target);
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification changeNotificationStatus(UUID notificationId, NotificationStatus notificationStatus) {
        if(notificationId == null) {
            throw new IllegalArgumentException("notificationId must not be null.");
        }
        if(notificationStatus == null) {
            throw new IllegalArgumentException("notificationStatus must not be null.");
        }
        log.info("Changing notification status with id: {}", notificationId);
        Notification notification = getNotificationById(notificationId);
        notification.setNotificationStatus(notificationStatus);
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification changeNotificationPriority(UUID notificationId, int priority) {
        if(notificationId == null) {
            throw new IllegalArgumentException("notificationId must not be null.");
        }
        if(priority < 0) {
            throw new IllegalArgumentException("priority must not be negative.");
        }
        log.info("Changing notification priority with id: {}", notificationId);
        Notification notification = getNotificationById(notificationId);
        notification.setPriority(priority);
        return notificationRepository.save(notification);
    }

    @Transactional
    public void deleteNotificationById(UUID notificationId) {
        if(notificationId == null) {
            throw new IllegalArgumentException("notificationId must not be null.");
        }
        log.info("Deleting notification by id: {}", notificationId);
        Notification notification = getNotificationById(notificationId);
        notificationRepository.delete(notification);
    }
}
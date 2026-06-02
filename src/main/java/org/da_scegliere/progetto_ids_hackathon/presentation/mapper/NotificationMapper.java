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

package org.da_scegliere.progetto_ids_hackathon.presentation.mapper;

import org.da_scegliere.progetto_ids_hackathon.core.entities.notification.AbstractNotification;
import org.da_scegliere.progetto_ids_hackathon.core.entities.notification.BaseNotification;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.notifications.NotificationResponse;

import java.util.ArrayList;
import java.util.List;

public class NotificationMapper {

    public static List<NotificationResponse> toNotificationResponseList(List<AbstractNotification> notifications) {
        return notifications.stream()
                .map(NotificationMapper::toNotificationResponse)
                .toList();
    }

    public static NotificationResponse toNotificationResponse(AbstractNotification notification) {

        Integer priority = null;

        if (notification instanceof BaseNotification base) {
            priority = base.getPriority();
        }

        return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getDate(),
                notification.getNotificationStatus(),
                priority,
                notification.getTarget().getId()
        );
    }

}

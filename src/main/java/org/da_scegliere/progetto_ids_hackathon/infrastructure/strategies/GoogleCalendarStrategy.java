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

package org.da_scegliere.progetto_ids_hackathon.infrastructure.strategies;

import org.da_scegliere.progetto_ids_hackathon.application.ports.strategies.CalendarStrategy;
import org.da_scegliere.progetto_ids_hackathon.application.services.views.calendar.CalendarBookedSlotView;
import org.da_scegliere.progetto_ids_hackathon.application.services.views.calendar.CalendarView;
import org.da_scegliere.progetto_ids_hackathon.core.entities.staff.StaffAssignment;
import org.da_scegliere.progetto_ids_hackathon.core.entities.support.SupportRequest;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Team;
import org.da_scegliere.progetto_ids_hackathon.infrastructure.strategies.dto.ExternalCalendarBookedSlotPayload;
import org.da_scegliere.progetto_ids_hackathon.infrastructure.strategies.dto.ExternalCalendarPayload;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Primary
@Component
public class GoogleCalendarStrategy implements CalendarStrategy {

    /**
     * TODO: integrate Google Calendar API.
     */
    @Override
    public boolean isSlotAvailable(SupportRequest request) {
        return true;
    }

    /**
     * TODO: integrate Google Calendar API.
     */
    @Override
    public void reserveCallSlot(SupportRequest request) {
        // Intentionally left blank until provider integration is implemented.
    }

    @Override
    public CalendarView getTeamCalendar(Team team) {
        ExternalCalendarPayload payload = fetchTeamCalendarPayload(team);
        return mapToCalendarView("TEAM", team.getId(), team.getName(), payload);
    }

    @Override
    public CalendarView getMentorCalendar(StaffAssignment mentorAssignment) {
        String mentorName = mentorAssignment.getStaffMember() != null
                ? mentorAssignment.getStaffMember().getName()
                : "Mentor";
        ExternalCalendarPayload payload = fetchMentorCalendarPayload(mentorAssignment);
        return mapToCalendarView("MENTOR", mentorAssignment.getId(), mentorName, payload);
    }

    private ExternalCalendarPayload fetchTeamCalendarPayload(Team team) {
        // TODO: replace stub with JSON returned by the external calendar provider.
        return new ExternalCalendarPayload(team.getName(), List.of());
    }

    private ExternalCalendarPayload fetchMentorCalendarPayload(StaffAssignment mentorAssignment) {
        // TODO: replace stub with JSON returned by the external calendar provider.
        String mentorName = mentorAssignment.getStaffMember() != null
                ? mentorAssignment.getStaffMember().getName()
                : "Mentor";
        return new ExternalCalendarPayload(mentorName, List.of());
    }

    private CalendarView mapToCalendarView(
            String ownerType,
            java.util.UUID ownerId,
            String fallbackOwnerName,
            ExternalCalendarPayload payload
    ) {
        String ownerName = payload.ownerName() == null || payload.ownerName().isBlank()
                ? fallbackOwnerName
                : payload.ownerName();

        List<CalendarBookedSlotView> slots = payload.bookedSlots() == null
                ? List.of()
                : payload.bookedSlots().stream()
                .map(this::mapSlot)
                .toList();

        return new CalendarView(ownerType, ownerId, ownerName, slots);
    }

    private CalendarBookedSlotView mapSlot(ExternalCalendarBookedSlotPayload payload) {
        return new CalendarBookedSlotView(
                payload.supportRequestId(),
                payload.teamId(),
                payload.mentorAssignmentId(),
                payload.mentorName(),
                payload.startAt(),
                payload.endAt(),
                payload.title()
        );
    }

}

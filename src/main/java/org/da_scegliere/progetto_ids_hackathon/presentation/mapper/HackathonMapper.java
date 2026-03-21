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

package org.da_scegliere.progetto_ids_hackathon.presentation.mapper;

import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.Hackathon;
import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.HackathonTimeline;
import org.da_scegliere.progetto_ids_hackathon.core.entities.staff.StaffAssignment;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Team;
import org.da_scegliere.progetto_ids_hackathon.core.entities.user.AbstractUser;
import org.da_scegliere.progetto_ids_hackathon.core.enums.StaffRole;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.staff.StaffAssignmentInputRequest;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.hackathon.FullHackathonResponse;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.hackathon.PublicHackathonResponse;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.staff.StaffAssignmentResponse;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.team.TeamResponse;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HackathonMapper{

    public static PublicHackathonResponse toPublic( Hackathon hackathon ){
        return new PublicHackathonResponse(
                hackathon.getId(),
                hackathon.getName(),
                hackathon.getDescription(),
                registrationDeadlineOf(hackathon)
        );
    }

    public static FullHackathonResponse toFull( Hackathon hackathon ){
        return new FullHackathonResponse(
                hackathon.getId(),
                hackathon.getName(),
                hackathon.getDescription(),
                hackathon.getAwardPrize(),
                toTeam(hackathon.getWinner()),
                toStaffAssignmentList(hackathon.getStaff()),
                registrationDeadlineOf(hackathon),
                submissionDeadlineOf(hackathon),
                evaluationDeadlineOf(hackathon)
        );
    }

    public static Map<UUID, StaffRole> toStaffMap(List<StaffAssignmentInputRequest> assignments) {
        Map<UUID, StaffRole> staffMap = new LinkedHashMap<>();
        for (StaffAssignmentInputRequest assignment : assignments) {
            StaffRole existingRole = staffMap.putIfAbsent(assignment.staffMemberId(), assignment.role());
            if (existingRole != null && existingRole != assignment.role()) {
                throw new IllegalArgumentException("Duplicate staffMemberId with conflicting roles: " + assignment.staffMemberId());
            }
        }
        return staffMap;
    }

    private static TeamResponse toTeam( Team team){
        if (team == null) {
            return null;
        }
        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getMembers().stream()
                        .map(AbstractUser::getId)
                        .toList()
        );
    }

    private static List<StaffAssignmentResponse> toStaffAssignmentList(List<StaffAssignment> staffAssignments){
        if (staffAssignments == null) return List.of();

        return staffAssignments.stream()
                .map(staffAssignment -> new StaffAssignmentResponse(
                        staffAssignment.getStaffMember() != null ? staffAssignment.getStaffMember().getId() : null,
                        staffAssignment.getStaffRole().name()
                ))
                .toList();
    }

    private static LocalDate registrationDeadlineOf(Hackathon hackathon) {
        HackathonTimeline timeline = hackathon.getTimeline();
        return timeline == null ? null : timeline.registrationDeadline();
    }

    private static LocalDate submissionDeadlineOf(Hackathon hackathon) {
        HackathonTimeline timeline = hackathon.getTimeline();
        return timeline == null ? null : timeline.submissionDeadline();
    }

    private static LocalDate evaluationDeadlineOf(Hackathon hackathon) {
        HackathonTimeline timeline = hackathon.getTimeline();
        return timeline == null ? null : timeline.evaluationDeadline();
    }
}

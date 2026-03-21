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
import org.da_scegliere.progetto_ids_hackathon.core.entities.staff.StaffMember;
import org.da_scegliere.progetto_ids_hackathon.core.enums.StaffRole;
import org.da_scegliere.progetto_ids_hackathon.presentation.controller.StaffController;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.hackathon.HackathonSummaryResponse;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.staff.StaffMemberResponse;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class StaffMapper{
    public static StaffMemberResponse toResponse( StaffMember staffMember) {
        return new StaffMemberResponse(
                staffMember.getId(),
                staffMember.getName(),
                staffMember.getAge(),
                staffMember.getEmail(),
                staffMember.getAccountStatus()
        );
    }

    public static HackathonSummaryResponse toHackathonSummary( Hackathon hackathon) {
        return new HackathonSummaryResponse(
                hackathon.getId(),
                hackathon.getName(),
                hackathon.getDescription(),
                hackathon.getRegistrationDeadline(),
                hackathon.getSubmissionDeadline(),
                hackathon.getEvaluationDeadline()
        );
    }

    public static Map<StaffRole, List<HackathonSummaryResponse>> toGroupedResponse(
            Map<StaffRole, List<Hackathon>> groupedHackathons
    ) {
        Map<StaffRole, List<HackathonSummaryResponse>> response = new EnumMap<>(StaffRole.class);
        for (Map.Entry<StaffRole, List<Hackathon>> entry : groupedHackathons.entrySet()) {
            response.put(entry.getKey(), entry.getValue().stream().map(StaffMapper::toHackathonSummary).toList());
        }
        return response;
    }
}

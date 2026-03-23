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

package org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record CreateTeamRequest(
        UUID creatorId,
        @NotEmpty List<@Valid UserInputRequest> teamMembers,
        @NotEmpty String teamName
) {

    public UUID resolvedCreatorId() {
        if (creatorId != null) {
            return creatorId;
        }
        if (teamMembers == null || teamMembers.isEmpty()) {
            return null;
        }
        return teamMembers.getFirst().userId();
    }

    public List<UUID> resolvedInviteeIds() {
        List<UUID> members = teamMembers == null
                ? List.of()
                : teamMembers.stream().map(UserInputRequest::userId).toList();

        UUID resolvedCreator = resolvedCreatorId();
        if (resolvedCreator == null) {
            return List.of();
        }

        List<UUID> invitees = new ArrayList<>();
        boolean creatorRemovedInLegacyMode = false;
        for (UUID memberId : members) {
            if (!creatorRemovedInLegacyMode && creatorId == null && resolvedCreator.equals(memberId)) {
                creatorRemovedInLegacyMode = true;
                continue;
            }
            if (!resolvedCreator.equals(memberId)) {
                invitees.add(memberId);
            }
        }
        return List.copyOf(invitees);
    }
}

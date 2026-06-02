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

package org.da_scegliere.progetto_ids_hackathon.core.policies.team;

import org.da_scegliere.progetto_ids_hackathon.core.exceptions.team.TeamMinimumMembersViolationException;
import org.da_scegliere.progetto_ids_hackathon.core.policies.BusinessPolicy;
import org.da_scegliere.progetto_ids_hackathon.core.policies.PolicyRule;

import java.util.List;
import java.util.Objects;

public class LeaveTeamPolicy implements BusinessPolicy<LeaveTeamContext> {

    private final List<PolicyRule<LeaveTeamContext>> rules = List.of(
            new PolicyRule<>(
                    context ->
                            context.team().getMembers().size() > 2,
                    context ->
                            new TeamMinimumMembersViolationException()
            )
    );

    /**
     * Validates the removal of a team member and throws on first violated rule.
     *
     * @param context team context to validate.
     * @throws TeamMinimumMembersViolationException when a rule is violated.
     */
    @Override
    public void validate(LeaveTeamContext context) {
        Objects.requireNonNull(context, "context must not be null.");
        for (PolicyRule<LeaveTeamContext> rule : rules) {
            rule.verify(context);
        }
    }
}

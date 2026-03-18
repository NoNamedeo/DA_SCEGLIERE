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

package org.da_scegliere.progetto_ids_hackathon.core.policies.support;

import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.supportRequest.InvalidSupportRequestMentorSelectionException;
import org.da_scegliere.progetto_ids_hackathon.core.entities.staff.StaffAssignment;
import org.da_scegliere.progetto_ids_hackathon.core.policies.BusinessPolicy;
import org.da_scegliere.progetto_ids_hackathon.core.policies.PolicyRule;
import org.da_scegliere.progetto_ids_hackathon.core.policies.Specification;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Policy composed of specifications validating mentor selection for support requests.
 */
public final class SupportRequestMentorSelectionPolicy implements BusinessPolicy<SupportRequestMentorSelectionContext>{

    private static final Specification<SupportRequestMentorSelectionContext> SELECTED_MENTORS_PRESENT =
            context -> context.selectedMentors() != null && !context.selectedMentors().isEmpty();

    private static final Specification<SupportRequestMentorSelectionContext> TEAM_ENROLLED_IN_ANY_HACKATHON =
            context -> context.teamHackathonIds() != null && !context.teamHackathonIds().isEmpty();

    private static final Specification<SupportRequestMentorSelectionContext> MENTORS_MATCH_TEAM_HACKATHONS =
            context -> {
                List<StaffAssignment> selectedMentors = context.selectedMentors();
                Set<UUID> teamHackathonIds = context.teamHackathonIds();
                if (selectedMentors == null || teamHackathonIds == null) {
                    return false;
                }
                return selectedMentors.stream()
                        .allMatch(mentor -> mentor != null
                                && mentor.getHackathon() != null
                                && mentor.getHackathon().getId() != null
                                && teamHackathonIds.contains(mentor.getHackathon().getId()));
            };

    private final List<PolicyRule<SupportRequestMentorSelectionContext>> rules = List.of(
            new PolicyRule<>(
                    SELECTED_MENTORS_PRESENT,
                    context ->
                            new InvalidSupportRequestMentorSelectionException("selectedMentors must not be empty.")
            ),
            new PolicyRule<>(
                    TEAM_ENROLLED_IN_ANY_HACKATHON,
                    context ->
                            new InvalidSupportRequestMentorSelectionException("Team must be enrolled in at least one hackathon to request mentor support.")
            ),
            new PolicyRule<>(
                    SELECTED_MENTORS_PRESENT.and(TEAM_ENROLLED_IN_ANY_HACKATHON).and(MENTORS_MATCH_TEAM_HACKATHONS),
                    context ->
                            new InvalidSupportRequestMentorSelectionException("All selected mentors must be assigned to a hackathon where the team participates.")
            )
    );

    /**
     * Validates mentor selection and throws on first violated rule.
     *
     * @param context mentor-selection policy context.
     * @throws InvalidSupportRequestMentorSelectionException when any selection rule is violated.
     */
    @Override
    public void validate(SupportRequestMentorSelectionContext context) {
        Objects.requireNonNull(context, "context must not be null.");
        for (PolicyRule<SupportRequestMentorSelectionContext> rule : rules) {
            rule.verify(context);
        }
    }
}
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

import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.ISupportRequestRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.ITeamParticipationRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.ITeamRepository;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.SupportRequestNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.supportRequest.InvalidSupportRequestMentorSelectionException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.supportRequest.InvalidSupportRequestStateTransitionException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.team.TeamNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.core.entities.staff.StaffAssignment;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Team;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.TeamParticipation;
import org.da_scegliere.progetto_ids_hackathon.core.enums.states.support.SupportRequestState;
import org.da_scegliere.progetto_ids_hackathon.core.support.SupportRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application service for support-request lifecycle management.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Expose CRUD-style retrieval operations for support requests.</li>
 *     <li>Create requests from teams and validate selected mentors against team hackathon enrolments.</li>
 *     <li>Handle support-request state transitions (OPEN, IN_PROGRESS, RESOLVED, REJECTED).</li>
 *     <li>Translate invalid transition/selection scenarios into explicit application exceptions.</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class SupportRequestService {

    private final ISupportRequestRepository supportRequestRepository;
    private final ITeamRepository teamRepository;
    private final ITeamParticipationRepository teamParticipationRepository;

    /**
     * Creates a new service instance.
     *
     * @param supportRequestRepository repository for support-request persistence.
     * @param teamRepository repository used to validate and resolve teams.
     * @param teamParticipationRepository repository used to validate team enrolment in hackathons.
     * @throws NullPointerException when any dependency is {@code null}.
     */
    public SupportRequestService(
            ISupportRequestRepository supportRequestRepository,
            ITeamRepository teamRepository,
            ITeamParticipationRepository teamParticipationRepository
    ) {
        this.supportRequestRepository =
                Objects.requireNonNull(supportRequestRepository, "supportRequestRepository must not be null.");
        this.teamRepository =
                Objects.requireNonNull(teamRepository, "teamRepository must not be null.");
        this.teamParticipationRepository =
                Objects.requireNonNull(teamParticipationRepository, "teamParticipationRepository must not be null.");
    }

    /**
     * Retrieves all support requests currently stored.
     *
     * @return immutable snapshot of all support requests.
     */
    public List<SupportRequest> getAllSupportRequest() {
        return List.copyOf(supportRequestRepository.findAll());
    }

    /**
     * Retrieves a support request by its identifier.
     *
     * @param requestId support request identifier.
     * @return the requested support request.
     * @throws IllegalArgumentException when {@code requestId} is {@code null}.
     * @throws SupportRequestNotFoundException when the request does not exist.
     */
    public SupportRequest getSupportRequestById(Long requestId) {
        if (requestId == null) {
            throw new IllegalArgumentException("requestId must not be null.");
        }
        return supportRequestRepository.findById(requestId)
                .orElseThrow(() -> new SupportRequestNotFoundException(requestId));
    }

    /**
     * Retrieves all support requests created by a specific team.
     *
     * @param teamId team identifier.
     * @return immutable list of support requests created by the given team.
     * @throws IllegalArgumentException when {@code teamId} is {@code null}.
     * @throws TeamNotFoundException when the team does not exist.
     */
    public List<SupportRequest> getSupportRequestByTeam(UUID teamId) {
        if (teamId == null) {
            throw new IllegalArgumentException("teamId must not be null.");
        }

        ensureTeamExists(teamId);
        return List.copyOf(supportRequestRepository.findBySendingTeam_id(teamId));
    }

    /**
     * Creates a new support request for a team and selected mentors.
     * <p>
     * Validation ensures that selected mentors belong to hackathons where the team is enrolled.
     *
     * @param dateSlot requested support date.
     * @param sendingTeam team opening the support request.
     * @param staffAssignments mentors selected to handle the request.
     * @return persisted support request.
     * @throws IllegalArgumentException when date/team identifiers are invalid.
     * @throws TeamNotFoundException when the sending team does not exist.
     * @throws InvalidSupportRequestMentorSelectionException when selected mentors are invalid for team context.
     */
    @Transactional
    public SupportRequest createSupportRequest(LocalDate dateSlot, Team sendingTeam, List<StaffAssignment> staffAssignments) {
        if (dateSlot == null) {
            throw new IllegalArgumentException("dateSlot must not be null.");
        }
        if (dateSlot.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("dateSlot must be today or in the future.");
        }

        Team persistedTeam = ensureTeamExists(requireTeamId(sendingTeam));
        validateSelectedMentorsForTeamHackathon(persistedTeam, staffAssignments);

        SupportRequest request = new SupportRequest(dateSlot, persistedTeam, staffAssignments);
        return supportRequestRepository.save(request);
    }

    /**
     * Marks a support request as in progress and sets the accepting mentor.
     *
     * @param requestId support request identifier.
     * @param acceptingMentor mentor that accepted the request.
     * @return updated support request in {@code IN_PROGRESS} state.
     * @throws IllegalArgumentException when {@code acceptingMentor} is {@code null}.
     * @throws SupportRequestNotFoundException when the request does not exist.
     * @throws InvalidSupportRequestMentorSelectionException when mentor is not among selected mentors.
     * @throws InvalidSupportRequestStateTransitionException when transition is not allowed from current state.
     */
    @Transactional
    public SupportRequest markInProgress(Long requestId, StaffAssignment acceptingMentor) {
        if (acceptingMentor == null) {
            throw new IllegalArgumentException("acceptingMentor must not be null.");
        }

        SupportRequest request = getSupportRequestById(requestId);
        boolean mentorSelected = request.getSelectedMentors() != null
                && request.getSelectedMentors().stream().anyMatch(selected -> sameId(selected, acceptingMentor));

        if (!mentorSelected) {
            throw new InvalidSupportRequestMentorSelectionException(
                    "acceptingMentor must be one of the selected mentors for the support request."
            );
        }

        transitionRequest(request, SupportRequestState.IN_PROGRESS);
        request.acceptedBy(acceptingMentor);
        return request;
    }

    /**
     * Marks a support request as resolved.
     *
     * @param requestId support request identifier.
     * @return updated support request in {@code RESOLVED} state.
     * @throws SupportRequestNotFoundException when the request does not exist.
     * @throws InvalidSupportRequestStateTransitionException when transition is not allowed from current state.
     */
    @Transactional
    public SupportRequest resolveRequest(Long requestId) {
        SupportRequest request = getSupportRequestById(requestId);
        transitionRequest(request, SupportRequestState.RESOLVED);
        return request;
    }

    /**
     * Marks a support request as rejected.
     *
     * @param requestId support request identifier.
     * @return updated support request in {@code REJECTED} state.
     * @throws SupportRequestNotFoundException when the request does not exist.
     * @throws InvalidSupportRequestStateTransitionException when transition is not allowed from current state.
     */
    @Transactional
    public SupportRequest rejectRequest(Long requestId) {
        SupportRequest request = getSupportRequestById(requestId);
        transitionRequest(request, SupportRequestState.REJECTED);
        return request;
    }

    /**
     * Deletes a support request.
     *
     * @param requestId support request identifier.
     * @throws SupportRequestNotFoundException when the request does not exist.
     */
    @Transactional
    public void deleteSupportRequest(Long requestId) {
        supportRequestRepository.delete(getSupportRequestById(requestId));
    }

    private Team ensureTeamExists(UUID teamId) {
        Team team = teamRepository.findTeamById(teamId);
        if (team == null) {
            throw new TeamNotFoundException(teamId);
        }
        return team;
    }

    private static UUID requireTeamId(Team team) {
        if (team == null) {
            throw new IllegalArgumentException("sendingTeam must not be null.");
        }
        if (team.getId() == null) {
            throw new IllegalArgumentException("sendingTeam.id must not be null.");
        }
        return team.getId();
    }

    private void validateSelectedMentorsForTeamHackathon(Team team, List<StaffAssignment> selectedMentors) {
        if (selectedMentors == null || selectedMentors.isEmpty()) {
            throw new InvalidSupportRequestMentorSelectionException("selectedMentors must not be empty.");
        }

        List<TeamParticipation> teamParticipations = teamParticipationRepository.findByTeam_id(team.getId());
        if (teamParticipations.isEmpty()) {
            throw new InvalidSupportRequestMentorSelectionException(
                    "Team must be enrolled in at least one hackathon to request mentor support."
            );
        }

        Set<UUID> teamHackathonIds = teamParticipations.stream()
                .map(TeamParticipation::getHackathon)
                .filter(Objects::nonNull)
                .map(h -> h.getId())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        boolean invalidMentorFound = selectedMentors.stream().anyMatch(mentorAssignment ->
                mentorAssignment == null
                        || mentorAssignment.getHackathon() == null
                        || mentorAssignment.getHackathon().getId() == null
                        || !teamHackathonIds.contains(mentorAssignment.getHackathon().getId())
        );

        if (invalidMentorFound) {
            throw new InvalidSupportRequestMentorSelectionException(
                    "All selected mentors must be assigned to a hackathon where the team participates."
            );
        }
    }

    private static void transitionRequest(SupportRequest request, SupportRequestState targetState) {
        try {
            request.transitionTo(targetState);
        } catch (IllegalStateException ex) {
            throw new InvalidSupportRequestStateTransitionException(request.getState(), targetState);
        }
    }

    private static boolean sameId(StaffAssignment first, StaffAssignment second) {
        return first != null
                && second != null
                && first.getId() != null
                && Objects.equals(first.getId(), second.getId());
    }
}

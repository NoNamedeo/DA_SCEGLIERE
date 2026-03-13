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

import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IHackathonRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IStaffMemberRepository;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.hackathon.HackathonNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.hackathon.InvalidHackathonStateOperationException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.hackathon.InvalidHackathonStateTransitionException;
import org.da_scegliere.progetto_ids_hackathon.core.entities.Participation;
import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.Hackathon;
import org.da_scegliere.progetto_ids_hackathon.core.entities.staff.StaffAssignment;
import org.da_scegliere.progetto_ids_hackathon.core.entities.staff.StaffMember;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Team;
import org.da_scegliere.progetto_ids_hackathon.core.enums.StaffRole;
import org.da_scegliere.progetto_ids_hackathon.core.states.hackathon.HackathonState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class HackathonService {

    private final IHackathonRepository hackathonRepository;
    private final IStaffMemberRepository staffMemberRepository;

    @Autowired
    public HackathonService(IHackathonRepository hackathonRepository, IStaffMemberRepository staffMemberRepository) {
        this.hackathonRepository = Objects.requireNonNull(hackathonRepository, "hackathonRepository must not be null");
        this.staffMemberRepository = Objects.requireNonNull(staffMemberRepository, "staffMemberRepository must not be null");
    }

    /**
     * Returns all hackathons currently stored.
     */
    public List<Hackathon> getAllHackathons() {
        return List.copyOf(hackathonRepository.findAll());
    }

    /**
     * Returns one hackathon by id.
     *
     * @throws HackathonNotFoundException if no hackathon exists for the provided id.
     */
    public Hackathon getHackathonById(UUID hackathonId) {
        if (hackathonId == null) {
            throw new IllegalArgumentException("hackathonId must not be null");
        }
        return hackathonRepository.findById(hackathonId)
                .orElseThrow(() -> new HackathonNotFoundException(hackathonId));
    }

    /**
     * Returns one hackathon by the provided name.
     *
     * @throws HackathonNotFoundException if no hackathon exists for the provided name.
     */
    public Hackathon getHackathonByName(String name) {
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }

        return hackathonRepository.findHackathonByName(name)
                .orElseThrow(() -> new HackathonNotFoundException(name));
    }

    /**
     * Creates and persists a new hackathon with state {@code REGISTRATION}.
     */
    @Transactional
    public Hackathon createHackathon(String name, String description, List<Participation> participations, List<StaffAssignment> staffAssignments) {
        Hackathon hackathon = new Hackathon(name, description, participations, staffAssignments);
        return hackathonRepository.save(hackathon);
    }

    /**
     * Updates hackathon description.
     */
    @Transactional
    public Hackathon changeDescription(UUID hackathonId, String description) {
        Hackathon hackathon = getHackathonById(hackathonId);
        hackathon.setDescription(description);
        return hackathon;
    }

    /**
     * Replaces hackathon participation's with the provided list.
     */
    @Transactional
    public Hackathon changeParticipations(UUID hackathonId, List<Participation> participations) {
        Hackathon hackathon = getHackathonById(hackathonId);
        hackathon.setParticipations(participations);
        return hackathon;
    }

    /**
     * Replaces hackathon staff assignments with the provided list.
     */
    @Transactional
    public Hackathon changeStaff(UUID hackathonId, List<StaffAssignment> staffAssignments) {
        Hackathon hackathon = getHackathonById(hackathonId);
        hackathon.setStaff(staffAssignments);
        return hackathon;
    }

    /**
     * Applies a specific state transition to the hackathon.
     */
    @Transactional
    public Hackathon transitionHackathonState(UUID hackathonId, HackathonState targetState) {
        Hackathon hackathon = getHackathonById(hackathonId);

        try {
            hackathon.transitionTo(targetState);
        } catch (IllegalStateException ex) {
            throw new InvalidHackathonStateTransitionException(hackathon.getHackathonState(), targetState, ex);
        }

        return hackathon;
    }

    /**
     * Moves the hackathon to the next lifecycle state.
     *
     * @throws InvalidHackathonStateTransitionException if the next state isn't reachable.
     */
    @Transactional
    public Hackathon advanceHackathonState(UUID hackathonId) {
        Hackathon hackathon = getHackathonById(hackathonId);

        try {
            hackathon.advanceState();
        } catch (IllegalStateException ex) {
            throw new InvalidHackathonStateTransitionException(hackathon.getHackathonState(), null, ex);
        }

        return hackathon;
    }

    /**
     * Assigns a winner team to the hackathon.
     */
    @Transactional
    public Hackathon assignWinner(UUID hackathonId, Team winnerTeam) {
        Hackathon hackathon = getHackathonById(hackathonId);
        //TODO implementa logica di assegnamento vincitori
        return hackathon;
    }

    /**
     * Deletes a hackathon by id.
     */
    @Transactional
    public void deleteHackathon(UUID hackathonId) {
        Hackathon hackathon = getHackathonById(hackathonId);
        hackathonRepository.delete(hackathon);
    }

    /**
     * @param hackathonId
     * @return true if exists, else false.
     */
    public boolean existsById(UUID hackathonId) {
        if (hackathonId == null) {
            throw new IllegalArgumentException("hackathonId must not be null");
        }
        return hackathonRepository.existsById(hackathonId);
    }

    /**
     * adds a list of StaffMembers in the hackathon, using the StaffAssignation
     * object, declaring also the StaffRole of the members.
     */
    @Transactional
    public Hackathon addStaffMembers(UUID hackathonId, Map<UUID, StaffRole> staffMembersIdMap) {
        Hackathon hackathon = getHackathonById(hackathonId);
        HackathonState hackathonState = hackathon.getHackathonState();
        if (hackathonState == HackathonState.EVALUATION || hackathonState == HackathonState.ENDED) {
            throw new InvalidHackathonStateOperationException(hackathonState, "Adding staff members");
        }

        Map<StaffMember, StaffRole> staffMembersMap = new HashMap<>();
        for (UUID staffMemberId : staffMembersIdMap.keySet()) {
            StaffMember staffMember = staffMemberRepository.getStaffMemberById(staffMemberId);
            StaffRole staffMemberRole = staffMembersIdMap.get(staffMemberId);
            if(staffMember != null && !(staffMembersMap.keySet().contains(staffMember))) {
                staffMembersMap.put(staffMember, staffMemberRole);
            }
        }

        List<StaffAssignment> staffAssignments = new ArrayList<>();
        for (StaffMember staffMember : staffMembersMap.keySet()) {
            staffAssignments.add(new StaffAssignment(LocalDate.now(), staffMembersMap.get(staffMember), staffMember, hackathon));
        }

        staffAssignments.addAll(hackathon.getStaff());
        hackathon.setStaff(staffAssignments);
        return hackathon;
    }

    /**
     * deletes the staff members in the given list (if they are in the hackathon)
     */
    public Hackathon deleteStaffMembers(UUID hackathonId, List<UUID> staffMembersId) {
        Hackathon hackathon = getHackathonById(hackathonId);
        HackathonState hackathonState = hackathon.getHackathonState();
        if (hackathonState == HackathonState.EVALUATION || hackathonState == HackathonState.ENDED) {
            throw new InvalidHackathonStateOperationException(hackathonState, "Adding staff members");
        }

        List<StaffAssignment> staffAssignments = hackathon.getStaff();
        for (UUID staffMemberId : staffMembersId) {
            StaffMember staffMember = staffMemberRepository.getStaffMemberById(staffMemberId);
            for (StaffAssignment staffAssignment : staffAssignments) {
                StaffMember oldMember = staffAssignment.getStaffMember();
                if (oldMember == staffMember) {
                    staffAssignments.remove(staffAssignment);
                    break;
                }
            }
        }
        hackathon.setStaff(staffAssignments);
        return hackathon;
    }
}

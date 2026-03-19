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
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IManagerRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IModerationReportRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IStaffMemberRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IStaffReportRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IUserReportRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IUserRepository;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.moderation.ManagerNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.moderation.UserNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.moderation.UserReportNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.staff.StaffMemberNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.core.entities.moderation.ModerationReport;
import org.da_scegliere.progetto_ids_hackathon.core.entities.moderation.StaffReport;
import org.da_scegliere.progetto_ids_hackathon.core.entities.moderation.UserReport;
import org.da_scegliere.progetto_ids_hackathon.core.enums.report.ReporterType;
import org.da_scegliere.progetto_ids_hackathon.core.enums.state.report.UserReportState;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Dedicated application service for moderation report lifecycle operations.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ModerationReportService {

    private final IModerationReportRepository moderationReportRepository;
    private final IUserReportRepository userReportRepository;
    private final IStaffReportRepository staffReportRepository;
    private final IUserRepository userRepository;
    private final IStaffMemberRepository staffMemberRepository;
    private final IManagerRepository managerRepository;

    /**
     * Retrieves all moderation reports, regardless of target type.
     */
    public List<ModerationReport> getAllReports() {
        return List.copyOf(moderationReportRepository.findAll());
    }

    /**
     * Retrieves all open moderation reports, regardless of target type.
     */
    public List<ModerationReport> getOpenReports() {
        return List.copyOf(moderationReportRepository.findByState(UserReportState.OPEN));
    }

    /**
     * Retrieves all user reports.
     */
    public List<UserReport> getAllUserReports() {
        return List.copyOf(userReportRepository.findAll());
    }

    /**
     * Retrieves open user reports.
     */
    public List<UserReport> getOpenUserReports() {
        return List.copyOf(userReportRepository.findByState(UserReportState.OPEN));
    }

    /**
     * Retrieves all staff reports.
     */
    public List<StaffReport> getAllStaffReports() {
        return List.copyOf(staffReportRepository.findAll());
    }

    /**
     * Retrieves open staff reports.
     */
    public List<StaffReport> getOpenStaffReports() {
        return List.copyOf(staffReportRepository.findByState(UserReportState.OPEN));
    }

    /**
     * Retrieves one user report by identifier.
     *
     * @throws UserReportNotFoundException if no report is found.
     */
    public UserReport getUserReportById(UUID reportId) {
        if (reportId == null) {
            throw new IllegalArgumentException("reportId must not be null.");
        }
        return userReportRepository.findById(reportId)
                .orElseThrow(() -> new UserReportNotFoundException(reportId));
    }

    /**
     * Creates a report targeting a user.
     *
     * @throws IllegalArgumentException if the provided ids are not valid
     * @throws UserNotFoundException if the user is not found
     * @throws ManagerNotFoundException if the manager is not found
     * @throws StaffMemberNotFoundException if the staff member is not found
     */
    @Transactional
    public UserReport createUserReport(
            UUID reporterId,
            ReporterType reporterType,
            UUID reportedUserId,
            String title,
            String description
    ) {
        ensureReporterExists(reporterId, reporterType);
        ensureUserExists(reportedUserId);

        UserReport report = new UserReport(
                reporterId,
                reporterType,
                reportedUserId,
                title,
                description
        );
        return userReportRepository.save(report);
    }

    /**
     * Creates a report targeting a staff member.
     *
     * @throws IllegalArgumentException if the provided ids are not valid
     * @throws UserNotFoundException if the user is not found
     * @throws ManagerNotFoundException if the manager is not found
     * @throws StaffMemberNotFoundException if the staff member is not found
     */
    @Transactional
    public StaffReport createStaffReport(
            UUID reporterId,
            ReporterType reporterType,
            UUID reportedStaffMemberId,
            String title,
            String description
    ) {
        ensureReporterExists(reporterId, reporterType);
        ensureStaffMemberExists(reportedStaffMemberId);

        StaffReport report = new StaffReport(
                reporterId,
                reporterType,
                reportedStaffMemberId,
                title,
                description
        );
        return staffReportRepository.save(report);
    }

    private void ensureReporterExists(UUID reporterId, ReporterType reporterType) {
        if (reporterId == null) {
            throw new IllegalArgumentException("reporterId must not be null.");
        }
        if (reporterType == null) {
            throw new IllegalArgumentException("reporterType must not be null.");
        }

        switch (reporterType) {
            case USER -> ensureUserExists(reporterId);
            case STAFF -> ensureStaffMemberExists(reporterId);
            case MANAGER -> {
                if (!managerRepository.existsById(reporterId)) {
                    throw new ManagerNotFoundException(reporterId);
                }
            }
        }
    }

    private void ensureUserExists(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null.");
        }
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
    }

    private void ensureStaffMemberExists(UUID staffMemberId) {
        if (staffMemberId == null) {
            throw new IllegalArgumentException("staffMemberId must not be null.");
        }
        if (!staffMemberRepository.existsById(staffMemberId)) {
            throw new StaffMemberNotFoundException(staffMemberId);
        }
    }

}

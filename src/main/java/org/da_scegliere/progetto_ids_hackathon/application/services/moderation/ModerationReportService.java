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

package org.da_scegliere.progetto_ids_hackathon.application.services.moderation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IBugReportRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IHackathonRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IManagerRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IModerationReportRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IStaffMemberRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IStaffReportRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.ITeamParticipationReportRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.ITeamParticipationRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IUserReportRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IUserRepository;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.hackathon.HackathonNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.moderation.ManagerNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.moderation.ReportNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.user.UserNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.staff.StaffMemberNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.teamParticipation.TeamParticipationNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.core.entities.moderation.BugReport;
import org.da_scegliere.progetto_ids_hackathon.core.entities.moderation.ModerationReport;
import org.da_scegliere.progetto_ids_hackathon.core.entities.moderation.StaffReport;
import org.da_scegliere.progetto_ids_hackathon.core.entities.moderation.TeamParticipationReport;
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
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ModerationReportService {

    private final IModerationReportRepository moderationReportRepository;
    private final IBugReportRepository bugReportRepository;
    private final IUserReportRepository userReportRepository;
    private final IStaffReportRepository staffReportRepository;
    private final ITeamParticipationReportRepository teamParticipationReportRepository;
    private final IUserRepository userRepository;
    private final IStaffMemberRepository staffMemberRepository;
    private final ITeamParticipationRepository teamParticipationRepository;
    private final IManagerRepository managerRepository;
    private final IHackathonRepository hackathonRepository;

    /**
     * Retrieves all moderation reports, regardless of target type.
     */
    public List<ModerationReport> getAllReports() {
        log.debug("Retrieving all moderation reports.");
        List<ModerationReport> reports = List.copyOf(moderationReportRepository.findAll());
        log.debug("Retrieved {} moderation reports.", reports.size());
        return reports;
    }

    /**
     * Retrieves all open moderation reports, regardless of target type.
     */
    public List<ModerationReport> getOpenReports() {
        log.debug("Retrieving open moderation reports.");
        List<ModerationReport> reports = List.copyOf(moderationReportRepository.findByState(UserReportState.OPEN));
        log.debug("Retrieved {} open moderation reports.", reports.size());
        return reports;
    }

    /**
     * Retrieves all user reports.
     */
    public List<UserReport> getAllUserReports() {
        log.debug("Retrieving all user moderation reports.");
        List<UserReport> reports = List.copyOf(userReportRepository.findAll());
        log.debug("Retrieved {} user moderation reports.", reports.size());
        return reports;
    }

    /**
     * Retrieves open user reports.
     */
    public List<UserReport> getOpenUserReports() {
        log.debug("Retrieving open user moderation reports.");
        List<UserReport> reports = List.copyOf(userReportRepository.findByState(UserReportState.OPEN));
        log.debug("Retrieved {} open user moderation reports.", reports.size());
        return reports;
    }

    /**
     * Retrieves all bug reports.
     */
    public List<BugReport> getAllBugReports() {
        log.debug("Retrieving all bug moderation reports.");
        List<BugReport> reports = List.copyOf(bugReportRepository.findAll());
        log.debug("Retrieved {} bug moderation reports.", reports.size());
        return reports;
    }

    /**
     * Retrieves open bug reports.
     */
    public List<BugReport> getOpenBugReports() {
        log.debug("Retrieving open bug moderation reports.");
        List<BugReport> reports = List.copyOf(bugReportRepository.findByState(UserReportState.OPEN));
        log.debug("Retrieved {} open bug moderation reports.", reports.size());
        return reports;
    }

    /**
     * Retrieves all staff reports.
     */
    public List<StaffReport> getAllStaffReports() {
        log.debug("Retrieving all staff moderation reports.");
        List<StaffReport> reports = List.copyOf(staffReportRepository.findAll());
        log.debug("Retrieved {} staff moderation reports.", reports.size());
        return reports;
    }

    /**
     * Retrieves open staff reports.
     */
    public List<StaffReport> getOpenStaffReports() {
        log.debug("Retrieving open staff moderation reports.");
        List<StaffReport> reports = List.copyOf(staffReportRepository.findByState(UserReportState.OPEN));
        log.debug("Retrieved {} open staff moderation reports.", reports.size());
        return reports;
    }

    /**
     * Retrieves all team participation reports.
     */
    public List<TeamParticipationReport> getAllTeamParticipationReports() {
        log.debug("Retrieving all team participation moderation reports.");
        List<TeamParticipationReport> reports = List.copyOf(teamParticipationReportRepository.findAll());
        log.debug("Retrieved {} team participation moderation reports.", reports.size());
        return reports;
    }

    /**
     * Retrieves open team participation reports.
     */
    public List<TeamParticipationReport> getOpenTeamParticipationReports() {
        log.debug("Retrieving open team participation moderation reports.");
        List<TeamParticipationReport> reports = List.copyOf(teamParticipationReportRepository.findByState(UserReportState.OPEN));
        log.debug("Retrieved {} open team participation moderation reports.", reports.size());
        return reports;
    }

    /**
     * Retrieves reports related to a specific team participation.
     */
    public List<TeamParticipationReport> getReportsByTeamParticipationId(UUID teamParticipationId) {
        if (teamParticipationId == null) {
            throw new IllegalArgumentException("teamParticipationId must not be null.");
        }
        ensureTeamParticipationExists(teamParticipationId);
        return List.copyOf(teamParticipationReportRepository.findByReportedTeamParticipationId(teamParticipationId));
    }

    /**
     * Retrieves team participation reports related to a specific hackathon.
     */
    public List<TeamParticipationReport> getReportsByHackathonId(UUID hackathonId) {
        if (hackathonId == null) {
            throw new IllegalArgumentException("hackathonId must not be null.");
        }
        ensureHackathonExists(hackathonId);

        List<UUID> teamParticipationIds = teamParticipationRepository.findByHackathon_id(hackathonId).stream()
                .map(teamParticipation -> teamParticipation.getId())
                .toList();

        if (teamParticipationIds.isEmpty()) {
            return List.of();
        }

        return teamParticipationReportRepository.findAll().stream()
                .filter(report -> teamParticipationIds.contains(report.getReportedTeamParticipationId()))
                .toList();
    }

    /**
     * Retrieves one moderation report by identifier, regardless of target type.
     *
     * @throws ReportNotFoundException if no report is found.
     */
    public ModerationReport getReportById(UUID reportId) {
        if (reportId == null) {
            throw new IllegalArgumentException("reportId must not be null.");
        }
        log.debug("Retrieving moderation report reportId={}.", reportId);
        ModerationReport report = moderationReportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException(reportId));
        log.debug("Retrieved moderation report reportId={}.", reportId);
        return report;
    }

    /**
     * Retrieves one user report by identifier.
     *
     * @throws ReportNotFoundException if no report is found.
     */
    public UserReport getUserReportById(UUID reportId) {
        if (reportId == null) {
            throw new IllegalArgumentException("reportId must not be null.");
        }
        log.debug("Retrieving user moderation report reportId={}.", reportId);
        UserReport report = userReportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException(reportId));
        log.debug("Retrieved user moderation report reportId={}.", reportId);
        return report;
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
        log.info(
                "Creating user moderation report reporterId={} reporterType={} reportedUserId={}.",
                reporterId,
                reporterType,
                reportedUserId
        );
        ensureReporterExists(reporterId, reporterType);
        ensureUserExists(reportedUserId);

        UserReport report = new UserReport(
                reporterId,
                reporterType,
                reportedUserId,
                title,
                description
        );
        UserReport savedReport = userReportRepository.save(report);
        log.info("Created user moderation report reportId={}.", savedReport.getId());
        return savedReport;
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
        log.info(
                "Creating staff moderation report reporterId={} reporterType={} reportedStaffMemberId={}.",
                reporterId,
                reporterType,
                reportedStaffMemberId
        );
        ensureReporterExists(reporterId, reporterType);
        ensureStaffMemberExists(reportedStaffMemberId);

        StaffReport report = new StaffReport(
                reporterId,
                reporterType,
                reportedStaffMemberId,
                title,
                description
        );
        StaffReport savedReport = staffReportRepository.save(report);
        log.info("Created staff moderation report reportId={}.", savedReport.getId());
        return savedReport;
    }

    /**
     * Creates a report targeting a bug.
     *
     * @throws IllegalArgumentException if the provided ids are not valid
     * @throws UserNotFoundException if the user is not found
     * @throws ManagerNotFoundException if the manager is not found
     * @throws StaffMemberNotFoundException if the staff member is not found
     */
    @Transactional
    public BugReport createBugReport(
            UUID reporterId,
            ReporterType reporterType,
            String title,
            String description
    ) {
        log.info(
                "Creating bug moderation report reporterId={} reporterType={}.",
                reporterId,
                reporterType
        );
        ensureReporterExists(reporterId, reporterType);

        BugReport report = new BugReport(
                reporterId,
                reporterType,
                title,
                description
        );
        BugReport savedReport = bugReportRepository.save(report);
        log.info("Created bug moderation report reportId={}.", savedReport.getId());
        return savedReport;
    }

    /**
     * Creates a report targeting one team participation in one hackathon.
     */
    @Transactional
    public TeamParticipationReport createTeamParticipationReport(
            UUID reporterId,
            ReporterType reporterType,
            UUID reportedTeamParticipationId,
            String title,
            String description
    ) {
        log.info(
                "Creating team participation moderation report reporterId={} reporterType={} reportedTeamParticipationId={}.",
                reporterId,
                reporterType,
                reportedTeamParticipationId
        );
        ensureReporterExists(reporterId, reporterType);
        ensureTeamParticipationExists(reportedTeamParticipationId);

        TeamParticipationReport report = new TeamParticipationReport(
                reporterId,
                reporterType,
                reportedTeamParticipationId,
                title,
                description
        );
        TeamParticipationReport savedReport = teamParticipationReportRepository.save(report);
        log.info("Created team participation moderation report reportId={}.", savedReport.getId());
        return savedReport;
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

    private void ensureTeamParticipationExists(UUID teamParticipationId) {
        if (teamParticipationId == null) {
            throw new IllegalArgumentException("teamParticipationId must not be null.");
        }
        if (teamParticipationRepository.findById(teamParticipationId).isEmpty()) {
            throw new TeamParticipationNotFoundException(teamParticipationId);
        }
    }

    private void ensureHackathonExists(UUID hackathonId) {
        if (hackathonId == null) {
            throw new IllegalArgumentException("hackathonId must not be null.");
        }
        if (!hackathonRepository.existsById(hackathonId)) {
            throw new HackathonNotFoundException(hackathonId);
        }
    }

}

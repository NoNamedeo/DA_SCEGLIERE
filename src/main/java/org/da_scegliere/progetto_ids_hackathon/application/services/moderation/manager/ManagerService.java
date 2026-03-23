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

package org.da_scegliere.progetto_ids_hackathon.application.services.moderation.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.da_scegliere.progetto_ids_hackathon.application.ports.events.DomainEventPublisher;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.*;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.moderation.*;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.staff.StaffEmailAlreadyInUseException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.user.*;
import org.da_scegliere.progetto_ids_hackathon.core.entities.moderation.ModerationReport;
import org.da_scegliere.progetto_ids_hackathon.core.entities.moderation.UserReport;
import org.da_scegliere.progetto_ids_hackathon.core.entities.user.Manager;
import org.da_scegliere.progetto_ids_hackathon.core.entities.user.User;
import org.da_scegliere.progetto_ids_hackathon.core.enums.state.report.UserReportState;
import org.da_scegliere.progetto_ids_hackathon.core.state.user.AccountLifecycleStateMachine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Application service that orchestrates manager moderation use cases.
 * <p>
 * Design note:
 * <ul>
 *     <li>Generic report listing uses the abstract {@link ModerationReport} type.</li>
 *     <li>User suspension-from-report remains on concrete {@link UserReport} because it targets users.</li>
 * </ul>
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ManagerService {

    private final IManagerRepository managerRepository;
    private final IUserRepository userRepository;
    private final IModerationReportRepository moderationReportRepository;
    private final IUserReportRepository userReportRepository;
    private final IStaffMemberRepository staffMemberRepository;
    private final AccountLifecycleStateMachine accountStateMachine;
    private final DomainEventPublisher domainEventPublisher;

    /**
     * Retrieves all moderation reports, regardless of specific target type.
     *
     * @param managerId manager identifier performing the operation.
     * @return immutable snapshot of all moderation reports.
     */
    public List<ModerationReport> getAllReports(UUID managerId) {
        log.debug("Retrieving all moderation reports for managerId={}.", managerId);
        ensureManagerExists(managerId);
        List<ModerationReport> reports = List.copyOf(moderationReportRepository.findAll());
        log.debug("Retrieved {} moderation reports for managerId={}.", reports.size(), managerId);
        return reports;
    }

    /**
     * Retrieves open moderation reports, regardless of specific target type.
     *
     * @param managerId manager identifier performing the operation.
     * @return immutable snapshot of open moderation reports.
     */
    public List<ModerationReport> getOpenReports(UUID managerId) {
        log.debug("Retrieving open moderation reports for managerId={}.", managerId);
        ensureManagerExists(managerId);
        List<ModerationReport> reports = List.copyOf(moderationReportRepository.findByState(UserReportState.OPEN));
        log.debug("Retrieved {} open moderation reports for managerId={}.", reports.size(), managerId);
        return reports;
    }

    /**
     * Backward-compatible method: retrieves all user reports only.
     *
     * @param managerId manager identifier performing the operation.
     * @return immutable snapshot of all user reports.
     */
    public List<UserReport> getAllUserReports(UUID managerId) {
        log.debug("Retrieving all user moderation reports for managerId={}.", managerId);
        ensureManagerExists(managerId);
        List<UserReport> reports = List.copyOf(userReportRepository.findAll());
        log.debug("Retrieved {} user moderation reports for managerId={}.", reports.size(), managerId);
        return reports;
    }

    /**
     * Backward-compatible method: retrieves open user reports only.
     *
     * @param managerId manager identifier performing the operation.
     * @return immutable snapshot of open user reports.
     */
    public List<UserReport> getOpenUserReports(UUID managerId) {
        log.debug("Retrieving open user moderation reports for managerId={}.", managerId);
        ensureManagerExists(managerId);
        List<UserReport> reports = List.copyOf(userReportRepository.findByState(UserReportState.OPEN));
        log.debug("Retrieved {} open user moderation reports for managerId={}.", reports.size(), managerId);
        return reports;
    }

    /**
     * Suspends a user for moderation reasons.
     *
     * @param managerId manager identifier performing the operation.
     * @param userId user identifier to suspend.
     * @param suspensionReason reason inserted by manager in the moderation form.
     * @return updated suspended user.
     */
    @Transactional
    public User suspendUser(UUID managerId, UUID userId, String suspensionReason) {
        log.info("Suspending user userId={} requestedByManagerId={}.", userId, managerId);
        ensureManagerExists(managerId);
        User user = getUserOrThrow(userId);

        ensureSuspendable(user);
        user.suspend(suspensionReason, accountStateMachine);

        domainEventPublisher.publish(user.toSuspendedEvent(suspensionReason));

        log.info("Suspended user userId={} requestedByManagerId={}.", userId, managerId);
        return user;
    }

    /**
     * Suspends a user from an open user report and marks that report as accepted.
     *
     * @param managerId manager identifier performing the operation.
     * @param reportId report identifier selected by the manager.
     * @param suspensionReason reason for suspension.
     * @param reportResolutionNotes notes recorded on report resolution.
     * @return updated suspended user.
     */
    @Transactional(noRollbackFor = UserNotFoundException.class)
    public User suspendUserFromReport(
            UUID managerId,
            UUID reportId,
            String suspensionReason,
            String reportResolutionNotes
    ) {
        log.info("Suspending user from report reportId={} requestedByManagerId={}.", reportId, managerId);

        Manager manager = ensureManagerExists(managerId);
        UserReport report = getUserReportOrThrow(reportId);

        if (!report.isOpen()) {
            throw new UserReportAlreadyProcessedException(reportId);
        }

        UUID reportedUserId = report.getReportedUserId();
        User user = userRepository.findById(reportedUserId).orElse(null);

        if (user == null) {
            report.reject(manager, "Suspension cancelled: reported user does not exist anymore.");
            log.warn(
                    "Cannot suspend from report reportId={}: reported user userId={} not found.",
                    reportId,
                    reportedUserId
            );
            throw new UserNotFoundException(reportedUserId);
        }

        ensureSuspendable(user);

        user.suspend(suspensionReason, accountStateMachine);
        report.accept(manager, reportResolutionNotes);
        domainEventPublisher.publish(user.toSuspendedEvent(suspensionReason));

        log.info(
                "Suspended user userId={} requestedByManagerId={} viaReportId={}.",
                user.getId(),
                managerId,
                reportId
        );
        return user;
    }

    /**
     * Reinstates a suspended user.
     *
     * @param managerId manager identifier performing the operation.
     * @param userId user identifier to reinstate.
     * @param reinstatementReason reason inserted by manager in the moderation form.
     * @return updated active user.
     */
    @Transactional
    public User reinstateUser(UUID managerId, UUID userId, String reinstatementReason) {
        log.info("Reinstating user userId={} requestedByManagerId={}.", userId, managerId);

        ensureManagerExists(managerId);
        User user = getUserOrThrow(userId);

        if (user.isRevoked()) {
            throw new UserAccountRevokedException(userId);
        }
        if (!user.isSuspended()) {
            throw new UserNotSuspendedException(userId);
        }

        user.reinstate(reinstatementReason, accountStateMachine);

        log.info("Reinstated user userId={} requestedByManagerId={}.", userId, managerId);
        return user;
    }

    /**
     * Revokes a user account.
     *
     * @param managerId manager identifier performing the operation.
     * @param userId user identifier to revoke.
     * @param revocationReason reason inserted by manager in the moderation form.
     * @return updated revoked user.
     */
    @Transactional
    public User revokeAccount(UUID managerId, UUID userId, String revocationReason) {
        log.info("Revoking user account userId={} requestedByManagerId={}.", userId, managerId);

        ensureManagerExists(managerId);
        User user = getUserOrThrow(userId);

        if (user.isRevoked()) {
            throw new UserAlreadyRevokedException(userId);
        }

        user.revoke(revocationReason, accountStateMachine);

        log.info("Revoked user account userId={} requestedByManagerId={}.", userId, managerId);
        return user;
    }

    private Manager ensureManagerExists(UUID managerId) {
        if (managerId == null) {
            throw new IllegalArgumentException("managerId must not be null.");
        }
        return managerRepository.findById(managerId)
                .orElseThrow(() -> new ManagerNotFoundException(managerId));
    }

    private User getUserOrThrow(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null.");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private UserReport getUserReportOrThrow(UUID reportId) {
        if (reportId == null) {
            throw new IllegalArgumentException("reportId must not be null.");
        }
        return userReportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException(reportId));
    }

    private void ensureEmailIsAvailable(String email) {
        boolean alreadyUsed = staffMemberRepository.findByEmail(email).isPresent()
                || userRepository.findByEmail(email).isPresent()
                || managerRepository.findByEmail(email).isPresent();
        if (alreadyUsed) {
            throw new StaffEmailAlreadyInUseException(email);
        }
    }

    private static void ensureSuspendable(User user) {
        UUID userId = user.getId();
        if (user.isRevoked()) {
            throw new UserAccountRevokedException(userId);
        }
        if (user.isSuspended()) {
            throw new UserAlreadySuspendedException(userId);
        }
    }

}

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

import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IManagerRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IModerationReportRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IStaffMemberRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IUserReportRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IUserRepository;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.moderation.UserNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.moderation.ManagerNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.moderation.StaffEmailAlreadyInUseException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.moderation.UserAccountRevokedException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.moderation.UserAlreadyRevokedException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.moderation.UserAlreadySuspendedException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.moderation.UserNotSuspendedException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.moderation.UserReportAlreadyProcessedException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.moderation.UserReportNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.core.entities.moderation.ModerationReport;
import org.da_scegliere.progetto_ids_hackathon.core.entities.moderation.UserReport;
import org.da_scegliere.progetto_ids_hackathon.core.entities.staff.StaffMember;
import org.da_scegliere.progetto_ids_hackathon.core.entities.user.Manager;
import org.da_scegliere.progetto_ids_hackathon.core.entities.user.User;
import org.da_scegliere.progetto_ids_hackathon.core.enums.state.report.UserReportState;
import org.da_scegliere.progetto_ids_hackathon.core.state.user.AccountLifecycleStateMachine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
@Service
@Transactional(readOnly = true)
public class ManagerService {

    private final IManagerRepository managerRepository;
    private final IUserRepository userRepository;
    private final IModerationReportRepository moderationReportRepository;
    private final IUserReportRepository userReportRepository;
    private final IStaffMemberRepository staffMemberRepository;
    private final AccountLifecycleStateMachine accountStateMachine;

    /**
     * Creates a new service instance.
     *
     * @param managerRepository manager repository.
     * @param userRepository user repository.
     * @param moderationReportRepository generic moderation report repository.
     * @param userReportRepository user-report repository.
     * @param staffMemberRepository staff repository.
     * @param accountStateMachine account lifecycle state-machine dependency.
     */
    public ManagerService(
            IManagerRepository managerRepository,
            IUserRepository userRepository,
            IModerationReportRepository moderationReportRepository,
            IUserReportRepository userReportRepository,
            IStaffMemberRepository staffMemberRepository,
            AccountLifecycleStateMachine accountStateMachine
    ) {
        this.managerRepository = Objects.requireNonNull(managerRepository, "managerRepository must not be null.");
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository must not be null.");
        this.moderationReportRepository = Objects.requireNonNull(
                moderationReportRepository,
                "moderationReportRepository must not be null."
        );
        this.userReportRepository = Objects.requireNonNull(userReportRepository, "userReportRepository must not be null.");
        this.staffMemberRepository = Objects.requireNonNull(staffMemberRepository, "staffMemberRepository must not be null.");
        this.accountStateMachine = Objects.requireNonNull(accountStateMachine, "accountStateMachine must not be null.");
    }

    /**
     * Retrieves all moderation reports, regardless of specific target type.
     *
     * @param managerId manager identifier performing the operation.
     * @return immutable snapshot of all moderation reports.
     */
    public List<ModerationReport> getAllReports(UUID managerId) {
        ensureManagerExists(managerId);
        return List.copyOf(moderationReportRepository.findAll());
    }

    /**
     * Retrieves open moderation reports, regardless of specific target type.
     *
     * @param managerId manager identifier performing the operation.
     * @return immutable snapshot of open moderation reports.
     */
    public List<ModerationReport> getOpenReports(UUID managerId) {
        ensureManagerExists(managerId);
        return List.copyOf(moderationReportRepository.findByState(UserReportState.OPEN));
    }

    /**
     * Backward-compatible method: retrieves all user reports only.
     *
     * @param managerId manager identifier performing the operation.
     * @return immutable snapshot of all user reports.
     */
    public List<UserReport> getAllUserReports(UUID managerId) {
        ensureManagerExists(managerId);
        return List.copyOf(userReportRepository.findAll());
    }

    /**
     * Backward-compatible method: retrieves open user reports only.
     *
     * @param managerId manager identifier performing the operation.
     * @return immutable snapshot of open user reports.
     */
    public List<UserReport> getOpenUserReports(UUID managerId) {
        ensureManagerExists(managerId);
        return List.copyOf(userReportRepository.findByState(UserReportState.OPEN));
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
        ensureManagerExists(managerId);
        User user = getUserOrThrow(userId);

        ensureSuspendable(user);
        user.suspend(suspensionReason, accountStateMachine);
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
    @Transactional
    public User suspendUserFromReport(
            UUID managerId,
            UUID reportId,
            String suspensionReason,
            String reportResolutionNotes
    ) {
        Manager manager = ensureManagerExists(managerId);
        UserReport report = getUserReportOrThrow(reportId);
        if (!report.isOpen()) {
            throw new UserReportAlreadyProcessedException(reportId);
        }

        UUID reportedUserId = report.getReportedUserId();
        User user = userRepository.findById(reportedUserId).orElse(null);
        if (user == null) {
            report.reject(manager, "Suspension cancelled: reported user does not exist anymore.");
            throw new UserNotFoundException(reportedUserId);
        }

        ensureSuspendable(user);

        user.suspend(suspensionReason, accountStateMachine);
        report.accept(manager, reportResolutionNotes);
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
        ensureManagerExists(managerId);
        User user = getUserOrThrow(userId);

        if (user.isRevoked()) {
            throw new UserAccountRevokedException(userId);
        }
        if (!user.isSuspended()) {
            throw new UserNotSuspendedException(userId);
        }

        user.reinstate(reinstatementReason, accountStateMachine);
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
        ensureManagerExists(managerId);
        User user = getUserOrThrow(userId);

        if (user.isRevoked()) {
            throw new UserAlreadyRevokedException(userId);
        }

        user.revoke(revocationReason, accountStateMachine);
        return user;
    }

    /**
     * Creates a new staff account.
     *
     * @param managerId manager identifier performing the operation.
     * @param name staff name.
     * @param age staff age.
     * @param email staff email.
     * @return persisted staff member.
     */
    @Transactional
    public StaffMember createStaffAccount(UUID managerId, String name, int age, String email) {
        ensureManagerExists(managerId);

        ensureEmailIsAvailable(email);

        StaffMember staffMember = new StaffMember(name, age, email, new ArrayList<>());
        return staffMemberRepository.save(staffMember);
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
                .orElseThrow(() -> new UserReportNotFoundException(reportId));
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

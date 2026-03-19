package org.da_scegliere.progetto_ids_hackathon.application.services;

import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IManagerRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IUserRepository;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.moderation.UserNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.moderation.StaffEmailAlreadyInUseException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.moderation.UserAlreadyRevokedException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.moderation.UserAlreadySuspendedException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.moderation.UserNotSuspendedException;
import org.da_scegliere.progetto_ids_hackathon.core.entities.moderation.ModerationReport;
import org.da_scegliere.progetto_ids_hackathon.core.entities.moderation.UserReport;
import org.da_scegliere.progetto_ids_hackathon.core.entities.staff.StaffMember;
import org.da_scegliere.progetto_ids_hackathon.core.entities.user.Manager;
import org.da_scegliere.progetto_ids_hackathon.core.entities.user.User;
import org.da_scegliere.progetto_ids_hackathon.core.enums.report.ReporterType;
import org.da_scegliere.progetto_ids_hackathon.core.enums.state.report.UserReportState;
import org.da_scegliere.progetto_ids_hackathon.core.state.common.StateRegistry;
import org.da_scegliere.progetto_ids_hackathon.core.state.user.AccountLifecycleStateMachine;
import org.da_scegliere.progetto_ids_hackathon.core.state.user.DefaultAccountLifecycleStateMachine;
import org.da_scegliere.progetto_ids_hackathon.core.state.user.state.AccountLifecycleState;
import org.da_scegliere.progetto_ids_hackathon.core.state.user.state.ActiveAccountState;
import org.da_scegliere.progetto_ids_hackathon.core.state.user.state.RevokedAccountState;
import org.da_scegliere.progetto_ids_hackathon.core.state.user.state.SuspendedAccountState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManagerServiceTest {

    @Mock
    private IManagerRepository managerRepository;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private ModerationReportService moderationReportService;

    @Mock
    private StaffService staffService;

    private ManagerService managerService;
    private AccountLifecycleStateMachine accountStateMachine;

    private UUID managerId;

    @BeforeEach
    void setUp() {
        accountStateMachine = new DefaultAccountLifecycleStateMachine(
                new StateRegistry<>(
                        java.util.List.of(
                                new ActiveAccountState(),
                                new SuspendedAccountState(),
                                new RevokedAccountState()
                        ),
                        AccountLifecycleState::getState,
                        state -> "Unsupported account status: " + state + "."
                )
        );

        managerService = new ManagerService(
                managerRepository,
                userRepository,
                moderationReportService,
                staffService,
                accountStateMachine
        );
        managerId = UUID.randomUUID();
        when(managerRepository.findById(managerId)).thenReturn(Optional.of(new Manager()));
    }

    @Test
    void getAllReportsReturnsGenericModerationReports() {
        ModerationReport report = org.mockito.Mockito.mock(ModerationReport.class);
        when(moderationReportService.getAllReports()).thenReturn(java.util.List.of(report));

        java.util.List<ModerationReport> reports = managerService.getAllReports(managerId);

        assertEquals(1, reports.size());
    }

    @Test
    void suspendUserWhenUserIsAlreadySuspendedThrowsException() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        user.suspend("Already suspended.", accountStateMachine);

        assertThrows(
                UserAlreadySuspendedException.class,
                () -> managerService.suspendUser(managerId, userId, "Suspension reason")
        );
    }

    @Test
    void suspendUserWhenValidSuspendsUser() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User updated = managerService.suspendUser(managerId, userId, "Violation of platform rules");

        assertNotNull(updated);
        assertTrue(updated.isSuspended());
        assertEquals("Violation of platform rules", updated.getModerationNote());
    }

    @Test
    void suspendUserFromReportWhenReportedUserDoesNotExistRejectsReportAndThrowsException() {
        UUID reportId = UUID.randomUUID();
        UUID reportedUserId = UUID.randomUUID();
        UserReport report = new UserReport(
                UUID.randomUUID(),
                ReporterType.USER,
                reportedUserId,
                "Abuse report",
                "Offensive behaviour"
        );
        when(moderationReportService.getUserReportById(reportId)).thenReturn(report);
        when(userRepository.findById(reportedUserId)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> managerService.suspendUserFromReport(
                        managerId,
                        reportId,
                        "Accepted report, suspend account",
                        "Evidence validated"
                )
        );
        assertEquals(UserReportState.REJECTED, report.getState());
    }

    @Test
    void suspendUserFromReportWhenValidSuspendsUserAndAcceptsReport() {
        UUID reportId = UUID.randomUUID();
        UUID reportedUserId = UUID.randomUUID();
        UserReport report = new UserReport(
                UUID.randomUUID(),
                ReporterType.USER,
                reportedUserId,
                "Abuse report",
                "Hate speech"
        );
        User persistedReportedUser = new User();

        when(moderationReportService.getUserReportById(reportId)).thenReturn(report);
        when(userRepository.findById(reportedUserId)).thenReturn(Optional.of(persistedReportedUser));

        User updated = managerService.suspendUserFromReport(
                managerId,
                reportId,
                "Confirmed abuse",
                "Validated against moderation policy"
        );

        assertNotNull(updated);
        assertTrue(updated.isSuspended());
        assertEquals(UserReportState.ACCEPTED, report.getState());
    }

    @Test
    void reinstateUserWhenUserIsNotSuspendedThrowsException() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(
                UserNotSuspendedException.class,
                () -> managerService.reinstateUser(managerId, userId, "Appeal accepted")
        );
    }

    @Test
    void revokeAccountWhenUserIsAlreadyRevokedThrowsException() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        user.revoke("Initial revocation", accountStateMachine);

        assertThrows(
                UserAlreadyRevokedException.class,
                () -> managerService.revokeAccount(managerId, userId, "Second revocation attempt")
        );
    }

    @Test
    void createStaffAccountWhenEmailAlreadyExistsThrowsException() {
        String duplicatedEmail = "staff@example.com";
        when(staffService.createStaffMember("Alice", 30, duplicatedEmail))
                .thenThrow(new StaffEmailAlreadyInUseException(duplicatedEmail));

        assertThrows(
                StaffEmailAlreadyInUseException.class,
                () -> managerService.createStaffAccount(managerId, "Alice", 30, duplicatedEmail)
        );
        verify(staffService).createStaffMember("Alice", 30, duplicatedEmail);
    }

    @Test
    void createStaffAccountWhenValidPersistsStaffMember() {
        String email = "NewStaff@Example.com";
        StaffMember persisted = new StaffMember("Alice", 28, "newstaff@example.com", new java.util.ArrayList<>());
        when(staffService.createStaffMember("Alice", 28, email)).thenReturn(persisted);

        StaffMember created = managerService.createStaffAccount(managerId, "Alice", 28, email);

        assertNotNull(created);
        assertEquals("newstaff@example.com", created.getEmail());
        verify(staffService).createStaffMember("Alice", 28, email);
    }
}

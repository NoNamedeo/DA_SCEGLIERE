package org.da_scegliere.progetto_ids_hackathon.application.services;

import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IManagerRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IModerationReportRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IStaffMemberRepository;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IUserReportRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManagerServiceTest {

    @Mock
    private IManagerRepository managerRepository;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IModerationReportRepository moderationReportRepository;

    @Mock
    private IUserReportRepository userReportRepository;

    @Mock
    private IStaffMemberRepository staffMemberRepository;

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
                moderationReportRepository,
                userReportRepository,
                staffMemberRepository,
                accountStateMachine
        );
        managerId = UUID.randomUUID();
        when(managerRepository.findById(managerId)).thenReturn(Optional.of(new Manager()));
    }

    @Test
    void getAllReportsReturnsGenericModerationReports() {
        ModerationReport report = org.mockito.Mockito.mock(ModerationReport.class);
        when(moderationReportRepository.findAll()).thenReturn(java.util.List.of(report));

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
        User reportedUser = org.mockito.Mockito.mock(User.class);
        when(reportedUser.getId()).thenReturn(reportedUserId);

        UserReport report = new UserReport(reportedUser, "Abuse report", "Offensive behaviour");
        when(userReportRepository.findById(reportId)).thenReturn(Optional.of(report));
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
        User reportedUserSnapshot = org.mockito.Mockito.mock(User.class);
        when(reportedUserSnapshot.getId()).thenReturn(reportedUserId);

        UserReport report = new UserReport(reportedUserSnapshot, "Abuse report", "Hate speech");
        User persistedReportedUser = new User();

        when(userReportRepository.findById(reportId)).thenReturn(Optional.of(report));
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
        when(staffMemberRepository.findByEmail(duplicatedEmail)).thenReturn(Optional.of(new StaffMember()));

        assertThrows(
                StaffEmailAlreadyInUseException.class,
                () -> managerService.createStaffAccount(managerId, "Alice", 30, duplicatedEmail)
        );
        verify(staffMemberRepository, never()).save(any(StaffMember.class));
    }

    @Test
    void createStaffAccountWhenValidPersistsStaffMember() {
        String email = "NewStaff@Example.com";
        when(staffMemberRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(managerRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(staffMemberRepository.save(any(StaffMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StaffMember created = managerService.createStaffAccount(managerId, "Alice", 28, email);

        assertNotNull(created);
        assertEquals("newstaff@example.com", created.getEmail());
        verify(staffMemberRepository).save(any(StaffMember.class));
    }
}

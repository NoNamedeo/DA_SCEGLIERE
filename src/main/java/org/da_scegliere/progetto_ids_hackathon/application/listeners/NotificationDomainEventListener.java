package org.da_scegliere.progetto_ids_hackathon.application.listeners;

import lombok.RequiredArgsConstructor;
import org.da_scegliere.progetto_ids_hackathon.application.factory.NotificationFactory;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.INotificationRepository;
import org.da_scegliere.progetto_ids_hackathon.core.entities.notification.BaseNotification;
import org.da_scegliere.progetto_ids_hackathon.core.events.hackathon.HackathonConcludedEvent;
import org.da_scegliere.progetto_ids_hackathon.core.events.hackathon.HackathonStaffAssignedEvent;
import org.da_scegliere.progetto_ids_hackathon.core.events.payment.WinnerPrizePaidEvent;
import org.da_scegliere.progetto_ids_hackathon.core.events.staff.StaffMemberCreatedEvent;
import org.da_scegliere.progetto_ids_hackathon.core.events.staff.StaffMemberNameChangedEvent;
import org.da_scegliere.progetto_ids_hackathon.core.events.support.SupportRequestAcceptedEvent;
import org.da_scegliere.progetto_ids_hackathon.core.events.support.SupportRequestCreatedEvent;
import org.da_scegliere.progetto_ids_hackathon.core.events.support.SupportRequestRejectedEvent;
import org.da_scegliere.progetto_ids_hackathon.core.events.team.TeamCreatedEvent;
import org.da_scegliere.progetto_ids_hackathon.core.events.team.TeamDeletedAfterLeaveEvent;
import org.da_scegliere.progetto_ids_hackathon.core.events.team.TeamDeletedEvent;
import org.da_scegliere.progetto_ids_hackathon.core.events.team.TeamInvitationRejectedEvent;
import org.da_scegliere.progetto_ids_hackathon.core.events.team.TeamMemberAddedEvent;
import org.da_scegliere.progetto_ids_hackathon.core.events.team.TeamMemberRemovedEvent;
import org.da_scegliere.progetto_ids_hackathon.core.events.user.UserSuspendedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationDomainEventListener {

    private final INotificationRepository notificationRepository;
    private final NotificationFactory notificationFactory;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onTeamCreated(TeamCreatedEvent event) {
        saveAll(notificationFactory.forTeamCreated(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onTeamDeleted(TeamDeletedEvent event) {
        saveAll(notificationFactory.forTeamDeleted(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onTeamMemberAdded(TeamMemberAddedEvent event) {
        saveAll(notificationFactory.forTeamMemberAdded(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onTeamMemberRemoved(TeamMemberRemovedEvent event) {
        saveAll(notificationFactory.forTeamMemberRemoved(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onTeamDeletedAfterLeave(TeamDeletedAfterLeaveEvent event) {
        saveAll(notificationFactory.forTeamDeletedAfterLeave(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onStaffMemberCreated(StaffMemberCreatedEvent event) {
        notificationRepository.save(notificationFactory.forStaffMemberCreated(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onStaffMemberNameChanged(StaffMemberNameChangedEvent event) {
        notificationRepository.save(notificationFactory.forStaffMemberNameChanged(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onUserSuspended(UserSuspendedEvent event) {
        notificationRepository.save(notificationFactory.forUserSuspended(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onWinnerPrizePaid(WinnerPrizePaidEvent event) {
        saveAll(notificationFactory.forWinnerPrizePaid(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onHackathonStaffAssigned(HackathonStaffAssignedEvent event) {
        notificationRepository.save(notificationFactory.forHackathonStaffAssigned(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onHackathonConcluded(HackathonConcludedEvent event) {
        saveAll(notificationFactory.forHackathonConcluded(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onSupportRequestCreated(SupportRequestCreatedEvent event) {
        saveAll(notificationFactory.forSupportRequestCreated(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onSupportRequestAccepted(SupportRequestAcceptedEvent event) {
        saveAll(notificationFactory.forSupportRequestAccepted(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onSupportRequestRejected(SupportRequestRejectedEvent event) {
        saveAll(notificationFactory.forSupportRequestRejected(event));
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onTeamInvitationRejected(TeamInvitationRejectedEvent event) {
        notificationRepository.save(notificationFactory.forTeamInvitationRejected(event));
    }

    private void saveAll(Iterable<BaseNotification> notifications) {
        for (BaseNotification notification : notifications) {
            notificationRepository.save(notification);
        }
    }
}

package org.da_scegliere.progetto_ids_hackathon.application.factory;

import org.da_scegliere.progetto_ids_hackathon.core.entities.notification.BaseNotification;
import org.da_scegliere.progetto_ids_hackathon.core.entities.staff.StaffMember;
import org.da_scegliere.progetto_ids_hackathon.core.entities.user.User;
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

import java.util.ArrayList;
import java.util.List;

@Component
public class NotificationFactory {

    private static final int DEFAULT_PRIORITY = 3;

    public List<BaseNotification> forTeamCreated(TeamCreatedEvent event) {
        List<BaseNotification> notifications = new ArrayList<>();
        for (User member : event.members()) {
            notifications.add(new BaseNotification(
                    "Team creato",
                    "il team è stato creato",
                    member,
                    DEFAULT_PRIORITY
            ));
        }
        return List.copyOf(notifications);
    }

    public List<BaseNotification> forTeamDeleted(TeamDeletedEvent event) {
        List<BaseNotification> notifications = new ArrayList<>();
        for (User member : event.formerMembers()) {
            notifications.add(new BaseNotification(
                    "Team cancellato",
                    "il team: " + event.teamName() + " è stato cancellato",
                    member,
                    DEFAULT_PRIORITY
            ));
        }
        return List.copyOf(notifications);
    }

    public List<BaseNotification> forTeamMemberAdded(TeamMemberAddedEvent event) {
        List<BaseNotification> notifications = new ArrayList<>();
        for (User member : event.membersToNotify()) {
            notifications.add(new BaseNotification(
                    "Nuovo membro",
                    "il team ha un nuovo membro: " + event.newMember().getName(),
                    member,
                    DEFAULT_PRIORITY
            ));
        }
        return List.copyOf(notifications);
    }

    public List<BaseNotification> forTeamMemberRemoved(TeamMemberRemovedEvent event) {
        List<BaseNotification> notifications = new ArrayList<>();
        for (User member : event.remainingMembers()) {
            notifications.add(new BaseNotification(
                    "Membro uscito dal team",
                    "Il membro " + event.removedMember().getName()
                            + " ha abbandonato il team " + event.teamName() + ".",
                    member,
                    DEFAULT_PRIORITY
            ));
        }
        return List.copyOf(notifications);
    }

    public List<BaseNotification> forTeamDeletedAfterLeave(TeamDeletedAfterLeaveEvent event) {
        List<BaseNotification> notifications = new ArrayList<>();
        for (User member : event.membersToNotify()) {
            notifications.add(new BaseNotification(
                    "Team cancellato",
                    "Il team " + event.teamName() + " è stato cancellato dopo l'abbandono di "
                            + event.removedMember().getName() + ".",
                    member,
                    DEFAULT_PRIORITY
            ));
        }
        return List.copyOf(notifications);
    }

    public BaseNotification forStaffMemberCreated(StaffMemberCreatedEvent event) {
        StaffMember staffMember = event.staffMember();
        return new BaseNotification(
                "Creazione account staff",
                "Nuovo Staff account creato",
                staffMember,
                DEFAULT_PRIORITY
        );
    }

    public BaseNotification forStaffMemberNameChanged(StaffMemberNameChangedEvent event) {
        StaffMember staffMember = event.staffMember();
        return new BaseNotification(
                "Nome aggiornato",
                "Il nome del tuo account è stato aggiornato in: " + event.newName(),
                staffMember,
                DEFAULT_PRIORITY
        );
    }

    public BaseNotification forUserSuspended(UserSuspendedEvent event) {
        return new BaseNotification(
                "Sospensione account",
                "Il tuo account è stato sospeso per la seguente motivazione: " + event.suspensionReason(),
                event.user(),
                4
        );
    }

    public List<BaseNotification> forWinnerPrizePaid(WinnerPrizePaidEvent event) {
        List<BaseNotification> notifications = new ArrayList<>();
        for (User winner : event.winners()) {
            notifications.add(new BaseNotification(
                    "Pagamento effettuato",
                    "il pagamento é stato effettuato",
                    winner,
                    DEFAULT_PRIORITY
            ));
        }
        return List.copyOf(notifications);
    }

    public BaseNotification forHackathonStaffAssigned(HackathonStaffAssignedEvent event) {
        return new BaseNotification(
                "Nuovo incarico",
                "Sei stato assegnato a un hackathon come " + event.role(),
                event.staffMember(),
                2
        );
    }

    public List<BaseNotification> forHackathonConcluded(HackathonConcludedEvent event) {
        List<BaseNotification> notifications = new ArrayList<>();
        for (User participant : event.participants()) {
            notifications.add(new BaseNotification(
                    "Hackathon concluso",
                    "L'hackathon " + event.hackathonName()
                            + " si è concluso con team vincitore: " + event.winnerTeamName(),
                    participant,
                    4
            ));
        }
        return List.copyOf(notifications);
    }

    public List<BaseNotification> forSupportRequestCreated(SupportRequestCreatedEvent event) {
        List<BaseNotification> notifications = new ArrayList<>();
        for (StaffMember staffMember : event.recipients()) {
            notifications.add(new BaseNotification(
                    "Richiesta di supporto",
                    "Richiesta di supporto ricevuta dal team: " + event.sendingTeamName()
                            + ", per il: " + event.dateSlot().getDayOfMonth()
                            + ", " + event.dateSlot().getMonth(),
                    staffMember,
                    DEFAULT_PRIORITY
            ));
        }
        return List.copyOf(notifications);
    }

    public List<BaseNotification> forSupportRequestAccepted(SupportRequestAcceptedEvent event) {
        List<BaseNotification> notifications = new ArrayList<>();
        for (User teamMember : event.teamMembers()) {
            notifications.add(new BaseNotification(
                    "Richiesta di supporto accettata",
                    "Il mentore " + event.mentorName()
                            + " ha accettato la richiesta di supporto inviata dal tuo team.",
                    teamMember,
                    5
            ));
        }
        return List.copyOf(notifications);
    }

    public List<BaseNotification> forSupportRequestRejected(SupportRequestRejectedEvent event) {
        List<BaseNotification> notifications = new ArrayList<>();
        for (User teamMember : event.teamMembers()) {
            notifications.add(new BaseNotification(
                    "Richiesta di supporto rifiutata",
                    "Il mentore ha rifiutato la richiesta di supporto inviata dal tuo team.",
                    teamMember,
                    DEFAULT_PRIORITY
            ));
        }
        return List.copyOf(notifications);
    }

    public BaseNotification forTeamInvitationRejected(TeamInvitationRejectedEvent event) {
        return new BaseNotification(
                "Invito rifiutato",
                "L'utente " + event.invitee().getName()
                        + " ha rifiutato l'invito al team '" + event.teamName() + "'.",
                event.creator(),
                DEFAULT_PRIORITY
        );
    }
}

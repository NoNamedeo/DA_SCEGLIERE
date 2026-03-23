package org.da_scegliere.progetto_ids_hackathon.core.events.team;

import org.da_scegliere.progetto_ids_hackathon.core.entities.user.User;

public record TeamInvitationRejectedEvent(
        User creator,
        User invitee,
        String teamName
) {
}

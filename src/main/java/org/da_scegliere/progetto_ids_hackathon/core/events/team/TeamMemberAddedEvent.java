package org.da_scegliere.progetto_ids_hackathon.core.events.team;

import org.da_scegliere.progetto_ids_hackathon.core.entities.user.User;

import java.util.List;
import java.util.UUID;

public record TeamMemberAddedEvent(
        UUID teamId,
        String teamName,
        User newMember,
        List<User> membersToNotify
) {
    public TeamMemberAddedEvent {
        membersToNotify = List.copyOf(membersToNotify);
    }
}

package org.da_scegliere.progetto_ids_hackathon.core.events.team;

import org.da_scegliere.progetto_ids_hackathon.core.entities.user.User;

import java.util.List;
import java.util.UUID;

public record TeamCreatedEvent(
        UUID teamId,
        String teamName,
        List<User> members
) {
    public TeamCreatedEvent {
        members = List.copyOf(members);
    }
}

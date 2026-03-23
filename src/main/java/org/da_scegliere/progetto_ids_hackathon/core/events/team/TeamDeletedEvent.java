package org.da_scegliere.progetto_ids_hackathon.core.events.team;

import org.da_scegliere.progetto_ids_hackathon.core.entities.user.User;

import java.util.List;
import java.util.UUID;

public record TeamDeletedEvent(
        UUID teamId,
        String teamName,
        List<User> formerMembers
) {
    public TeamDeletedEvent {
        formerMembers = List.copyOf(formerMembers);
    }
}

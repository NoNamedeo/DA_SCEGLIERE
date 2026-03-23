package org.da_scegliere.progetto_ids_hackathon.core.events.team;

import org.da_scegliere.progetto_ids_hackathon.core.entities.user.User;

import java.util.List;
import java.util.UUID;

public record TeamMemberRemovedEvent(
        UUID teamId,
        String teamName,
        User removedMember,
        List<User> remainingMembers
) {
    public TeamMemberRemovedEvent {
        remainingMembers = List.copyOf(remainingMembers);
    }
}

package org.da_scegliere.progetto_ids_hackathon.core.events.support;

import org.da_scegliere.progetto_ids_hackathon.core.entities.user.User;

import java.util.List;

public record SupportRequestAcceptedEvent(
        String mentorName,
        List<User> teamMembers
) {
    public SupportRequestAcceptedEvent {
        teamMembers = List.copyOf(teamMembers);
    }
}

package org.da_scegliere.progetto_ids_hackathon.core.events.hackathon;

import org.da_scegliere.progetto_ids_hackathon.core.entities.user.User;

import java.util.List;

public record HackathonConcludedEvent(
        String hackathonName,
        String winnerTeamName,
        List<User> participants
) {
    public HackathonConcludedEvent {
        participants = List.copyOf(participants);
    }
}

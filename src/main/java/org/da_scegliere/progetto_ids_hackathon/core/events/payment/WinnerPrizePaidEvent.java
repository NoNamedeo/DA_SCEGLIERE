package org.da_scegliere.progetto_ids_hackathon.core.events.payment;

import org.da_scegliere.progetto_ids_hackathon.core.entities.user.User;

import java.util.List;

public record WinnerPrizePaidEvent(List<User> winners) {
    public WinnerPrizePaidEvent {
        winners = List.copyOf(winners);
    }
}

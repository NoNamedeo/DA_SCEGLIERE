package org.da_scegliere.progetto_ids_hackathon.core.events.user;

import org.da_scegliere.progetto_ids_hackathon.core.entities.user.User;

public record UserSuspendedEvent(User user, String suspensionReason) {
}

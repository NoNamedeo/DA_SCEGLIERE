package org.da_scegliere.progetto_ids_hackathon.core.exceptions.team;

import java.util.UUID;

public class UserWithoutTeamException extends RuntimeException {

    public UserWithoutTeamException(UUID userId) {
        super("User does not belong to any team: userId='" + userId + "'.");
    }
}

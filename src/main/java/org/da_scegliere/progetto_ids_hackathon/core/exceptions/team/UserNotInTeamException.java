package org.da_scegliere.progetto_ids_hackathon.core.exceptions.team;

import java.util.UUID;

public class UserNotInTeamException extends RuntimeException {

    public UserNotInTeamException(UUID userId) {
        super("User is not a member of the specified team: userId='" + userId + "'.");
    }
}

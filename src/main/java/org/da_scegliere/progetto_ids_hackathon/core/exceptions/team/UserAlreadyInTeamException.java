package org.da_scegliere.progetto_ids_hackathon.core.exceptions.team;

import java.util.UUID;

public class UserAlreadyInTeamException extends RuntimeException {

    public UserAlreadyInTeamException(UUID userId) {
        super("User is already a member of this team: userId='" + userId + "'.");
    }
}

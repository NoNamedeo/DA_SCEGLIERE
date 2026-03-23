package org.da_scegliere.progetto_ids_hackathon.core.exceptions.team;

import java.util.UUID;

public class UserAlreadyAssignedToAnotherTeamException extends RuntimeException {

    public UserAlreadyAssignedToAnotherTeamException(UUID userId) {
        super("User already belongs to another team: userId='" + userId + "'.");
    }
}

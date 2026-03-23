package org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.team;

import java.util.UUID;

public class TeamCreationRequestNotFoundException extends RuntimeException {

    public TeamCreationRequestNotFoundException(UUID requestId) {
        super("Team creation request with id '" + requestId + "' was not found.");
    }
}


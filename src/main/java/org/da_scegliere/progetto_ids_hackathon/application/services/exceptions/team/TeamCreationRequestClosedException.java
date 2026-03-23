package org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.team;

import java.util.UUID;

public class TeamCreationRequestClosedException extends RuntimeException {

    public TeamCreationRequestClosedException(UUID requestId) {
        super("Team creation request '" + requestId + "' is already closed.");
    }
}


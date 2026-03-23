package org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.team;

import java.util.UUID;

public class TeamInvitationAlreadyProcessedException extends RuntimeException {

    public TeamInvitationAlreadyProcessedException(UUID invitationId) {
        super("Team invitation '" + invitationId + "' has already been processed.");
    }
}


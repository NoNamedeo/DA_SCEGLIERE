package org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.teamParticipation;

import java.util.UUID;

public class TeamAlreadyParticipatingException extends RuntimeException {
    public TeamAlreadyParticipatingException(UUID teamId, UUID hackathonId) {
        super("Team '" + teamId + "' is already participating in hackathon '" + hackathonId + "'.");
    }
}

package org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.teamParticipation;

import java.util.UUID;

public class TeamParticipationAlreadyDisqualifiedException extends RuntimeException {
    public TeamParticipationAlreadyDisqualifiedException(UUID teamParticipationId) {
        super("Team participation " + teamParticipationId + " is already disqualified.");
    }
}

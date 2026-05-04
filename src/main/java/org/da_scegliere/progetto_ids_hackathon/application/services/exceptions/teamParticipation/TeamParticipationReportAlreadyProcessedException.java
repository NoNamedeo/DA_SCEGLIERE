package org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.teamParticipation;

import java.util.UUID;

public class TeamParticipationReportAlreadyProcessedException extends RuntimeException {
    public TeamParticipationReportAlreadyProcessedException(UUID reportId) {
        super("Team participation report " + reportId + " is already processed.");
    }
}

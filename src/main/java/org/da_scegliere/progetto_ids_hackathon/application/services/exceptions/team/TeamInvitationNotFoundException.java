package org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.team;

import java.util.UUID;

public class TeamInvitationNotFoundException extends RuntimeException {

    public TeamInvitationNotFoundException(UUID invitationId) {
        super("Team invitation with id '" + invitationId + "' was not found.");
    }

    public TeamInvitationNotFoundException(UUID invitationId, UUID inviteeId) {
        super("Team invitation with id '" + invitationId + "' for invitee '" + inviteeId + "' was not found.");
    }
}


package org.da_scegliere.progetto_ids_hackathon.core.exceptions.team;

import java.util.UUID;

public class DuplicateTeamMemberException extends RuntimeException {

    public DuplicateTeamMemberException(UUID userId) {
        super("Duplicate team member provided: userId='" + userId + "'.");
    }
}

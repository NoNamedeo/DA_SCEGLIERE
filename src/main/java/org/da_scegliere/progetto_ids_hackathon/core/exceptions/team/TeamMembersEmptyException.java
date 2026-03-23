package org.da_scegliere.progetto_ids_hackathon.core.exceptions.team;

public class TeamMembersEmptyException extends RuntimeException {

    public TeamMembersEmptyException() {
        super("Team must contain at least one member.");
    }
}

package org.da_scegliere.progetto_ids_hackathon.core.exceptions.team;

public class NullTeamMemberException extends RuntimeException {

    public NullTeamMemberException() {
        super("Team members list must not contain null values.");
    }
}

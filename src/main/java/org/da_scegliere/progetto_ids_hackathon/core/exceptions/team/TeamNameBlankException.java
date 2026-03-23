package org.da_scegliere.progetto_ids_hackathon.core.exceptions.team;

public class TeamNameBlankException extends RuntimeException {

    public TeamNameBlankException() {
        super("Team name must not be blank.");
    }
}

package org.da_scegliere.progetto_ids_hackathon.core.exceptions.team;

public class TeamMinimumMembersViolationException extends RuntimeException {

    public TeamMinimumMembersViolationException() {
        super("Team should have at least two members.");
    }
}

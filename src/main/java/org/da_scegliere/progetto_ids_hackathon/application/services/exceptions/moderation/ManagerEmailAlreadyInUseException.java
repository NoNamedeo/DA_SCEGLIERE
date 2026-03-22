package org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.moderation;

public class ManagerEmailAlreadyInUseException extends RuntimeException {
    public ManagerEmailAlreadyInUseException(String email) {
        super("Cannot create manager account: email '" + email + "' is already in use.");
    }
}

package org.da_scegliere.progetto_ids_hackathon.application.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Team-creation workflow parameters.
 */
@ConfigurationProperties(prefix = "app.team")
public class TeamCreationProperties {

    private int minMembers = 2;
    private int invitationTtlDays = 21;

    public int getMinMembers() {
        return minMembers;
    }

    public void setMinMembers(int minMembers) {
        this.minMembers = minMembers;
    }

    public int getInvitationTtlDays() {
        return invitationTtlDays;
    }

    public void setInvitationTtlDays(int invitationTtlDays) {
        this.invitationTtlDays = invitationTtlDays;
    }
}


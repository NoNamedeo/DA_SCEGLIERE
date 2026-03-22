package org.da_scegliere.progetto_ids_hackathon.application.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Instant;
import java.time.ZoneId;

/**
 * Application properties for time source configuration.
 */
@ConfigurationProperties(prefix = "app.clock")
public class ClockProperties {

    private Mode mode = Mode.SYSTEM;
    private ZoneId zone = ZoneId.systemDefault();
    private Instant fixedInstant;

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public ZoneId getZone() {
        return zone;
    }

    public void setZone(ZoneId zone) {
        this.zone = zone;
    }

    public Instant getFixedInstant() {
        return fixedInstant;
    }

    public void setFixedInstant(Instant fixedInstant) {
        this.fixedInstant = fixedInstant;
    }

    public enum Mode {
        SYSTEM,
        FIXED
    }
}


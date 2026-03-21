package org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon;

import jakarta.persistence.Embeddable;

import java.time.LocalDate;

@Embeddable
public record HackathonTimeline(
        LocalDate registrationDeadline,
        LocalDate submissionDeadline,
        LocalDate evaluationDeadline
) {

    public HackathonTimeline {
        if (registrationDeadline != null
                && submissionDeadline != null
                && registrationDeadline.isAfter(submissionDeadline)) {
            throw new IllegalArgumentException("registrationDeadline must be on or before submissionDeadline.");
        }
        if (submissionDeadline == null && evaluationDeadline != null) {
            throw new IllegalArgumentException("evaluationDeadline requires submissionDeadline.");
        }
        if (submissionDeadline != null
                && evaluationDeadline != null
                && submissionDeadline.isAfter(evaluationDeadline)) {
            throw new IllegalArgumentException("submissionDeadline must be on or before evaluationDeadline.");
        }
    }

    public static HackathonTimeline empty() {
        return new HackathonTimeline(null, null, null);
    }
}

package org.da_scegliere.progetto_ids_hackathon.presentation.error;

/**
 * Represents a single field-level validation issue.
 *
 * @param field         invalid field path
 * @param message       validation message
 * @param rejectedValue serialized rejected value, if available
 */
public record ApiFieldViolation(
        String field,
        String message,
        String rejectedValue
) {
}

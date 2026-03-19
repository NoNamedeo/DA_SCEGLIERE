package org.da_scegliere.progetto_ids_hackathon.presentation.error;

import java.time.Instant;
import java.util.List;

/**
 * Canonical API error payload returned by the global exception handler.
 *
 * @param timestamp  UTC instant when the error was generated
 * @param status     HTTP status code
 * @param error      HTTP reason phrase
 * @param code       stable machine-readable error code
 * @param message    user-facing error message
 * @param path       request path
 * @param errorId    correlation id for troubleshooting
 * @param violations optional validation violations
 */
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        String errorId,
        List<ApiFieldViolation> violations
) {
    public ApiErrorResponse {
        violations = violations == null ? List.of() : List.copyOf(violations);
    }
}

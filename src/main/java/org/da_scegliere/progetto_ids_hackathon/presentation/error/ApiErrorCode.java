package org.da_scegliere.progetto_ids_hackathon.presentation.error;

/**
 * Stable machine-readable API error codes.
 */
public enum ApiErrorCode {
    VALIDATION_ERROR,
    MALFORMED_REQUEST,
    BAD_REQUEST,
    RESOURCE_NOT_FOUND,
    CONFLICT,
    FORBIDDEN,
    METHOD_NOT_ALLOWED,
    UPSTREAM_SERVICE_ERROR,
    UPSTREAM_SERVICE_UNAVAILABLE,
    INTERNAL_SERVER_ERROR
}

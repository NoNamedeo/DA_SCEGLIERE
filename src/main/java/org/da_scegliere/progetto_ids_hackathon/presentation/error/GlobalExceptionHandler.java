package org.da_scegliere.progetto_ids_hackathon.presentation.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.da_scegliere.progetto_ids_hackathon.application.ports.strategies.exceptions.CalendarProviderConflictException;
import org.da_scegliere.progetto_ids_hackathon.application.ports.strategies.exceptions.CalendarProviderUnavailableException;
import org.da_scegliere.progetto_ids_hackathon.application.ports.strategies.exceptions.PaymentProviderException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.calendar.CalendarConflictException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.calendar.CalendarUnavailableException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.hackathon.HackathonNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.hackathon.InvalidHackathonStateOperationException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.hackathon.WinnerAssignmentNotAllowedException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.moderation.ManagerNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.moderation.ManagerEmailAlreadyInUseException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.staff.StaffEmailAlreadyInUseException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.staff.StaffAssignmentConflictException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.user.UserAccountRevokedException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.user.UserAlreadyRevokedException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.user.UserAlreadySuspendedException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.user.UserNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.user.UserNotSuspendedException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.user.UserReportAlreadyProcessedException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.moderation.ReportNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.payment.PaymentFailedException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.payment.WinnerNotProclaimedException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.supportRequest.InvalidSupportRequestMentorSelectionException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.supportRequest.InvalidSupportRequestStateTransitionException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.supportRequest.SupportRequestNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.staff.StaffMemberNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.team.TeamNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.team.TeamCreationRequestNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.team.TeamInvitationNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.team.TeamCreationRequestClosedException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.team.TeamInvitationAlreadyProcessedException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.teamParticipation.InvalidSubmissionEvaluationException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.teamParticipation.SubmissionDeadlineExceededException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.teamParticipation.SubmissionEvaluationNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.teamParticipation.SubmissionNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.teamParticipation.TeamAlreadyParticipatingException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.teamParticipation.TeamParticipationNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.core.exceptions.team.DuplicateTeamMemberException;
import org.da_scegliere.progetto_ids_hackathon.core.exceptions.team.NullTeamMemberException;
import org.da_scegliere.progetto_ids_hackathon.core.exceptions.team.TeamMembersEmptyException;
import org.da_scegliere.progetto_ids_hackathon.core.exceptions.team.TeamMinimumMembersViolationException;
import org.da_scegliere.progetto_ids_hackathon.core.exceptions.team.TeamNameBlankException;
import org.da_scegliere.progetto_ids_hackathon.core.exceptions.team.UserAlreadyAssignedToAnotherTeamException;
import org.da_scegliere.progetto_ids_hackathon.core.exceptions.team.UserAlreadyInTeamException;
import org.da_scegliere.progetto_ids_hackathon.core.exceptions.team.UserNotInTeamException;
import org.da_scegliere.progetto_ids_hackathon.core.exceptions.team.UserWithoutTeamException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Centralized REST exception handling.
 *
 * <p>It translates domain and infrastructure exceptions into consistent HTTP responses
 * with stable error codes and machine-readable payloads.</p>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String VALIDATION_FAILED_MESSAGE = "Request validation failed.";
    private static final String MALFORMED_REQUEST_MESSAGE =
            "Request body is malformed or contains invalid values.";
    private static final String INTERNAL_ERROR_MESSAGE =
            "An internal error occurred. Please contact support if the problem persists.";
    private static final int MAX_REJECTED_VALUE_LENGTH = 256;

    @ExceptionHandler({
            HackathonNotFoundException.class,
            ManagerNotFoundException.class,
            UserNotFoundException.class,
            ReportNotFoundException.class,
            SupportRequestNotFoundException.class,
            StaffMemberNotFoundException.class,
            TeamNotFoundException.class,
            TeamCreationRequestNotFoundException.class,
            TeamInvitationNotFoundException.class,
            SubmissionNotFoundException.class,
            TeamParticipationNotFoundException.class,
            SubmissionEvaluationNotFoundException.class
    })
    public ResponseEntity<ApiErrorResponse> handleNotFound(RuntimeException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                ApiErrorCode.RESOURCE_NOT_FOUND,
                ex.getMessage(),
                request,
                List.of(),
                ex,
                ErrorType.CLIENT
        );
    }

    @ExceptionHandler({
            InvalidHackathonStateOperationException.class,
            WinnerAssignmentNotAllowedException.class,
            UserAlreadySuspendedException.class,
            UserAlreadyRevokedException.class,
            UserNotSuspendedException.class,
            UserReportAlreadyProcessedException.class,
            StaffEmailAlreadyInUseException.class,
            StaffAssignmentConflictException.class,
            TeamCreationRequestClosedException.class,
            TeamInvitationAlreadyProcessedException.class,
            TeamMinimumMembersViolationException.class,
            UserAlreadyAssignedToAnotherTeamException.class,
            UserAlreadyInTeamException.class,
            ManagerEmailAlreadyInUseException.class,
            InvalidSupportRequestStateTransitionException.class,
            SubmissionDeadlineExceededException.class,
            TeamAlreadyParticipatingException.class,
            CalendarConflictException.class,
            CalendarProviderConflictException.class,
            WinnerNotProclaimedException.class
    })
    public ResponseEntity<ApiErrorResponse> handleConflict(RuntimeException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                ApiErrorCode.CONFLICT,
                ex.getMessage(),
                request,
                List.of(),
                ex,
                ErrorType.CLIENT
        );
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            TeamNameBlankException.class,
            TeamMembersEmptyException.class,
            DuplicateTeamMemberException.class,
            NullTeamMemberException.class,
            UserNotInTeamException.class,
            UserWithoutTeamException.class,
            InvalidSupportRequestMentorSelectionException.class,
            InvalidSubmissionEvaluationException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBadRequest(RuntimeException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ApiErrorCode.BAD_REQUEST,
                ex.getMessage(),
                request,
                List.of(),
                ex,
                ErrorType.CLIENT
        );
    }

    @ExceptionHandler(UserAccountRevokedException.class)
    public ResponseEntity<ApiErrorResponse> handleForbidden(UserAccountRevokedException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                ApiErrorCode.FORBIDDEN,
                ex.getMessage(),
                request,
                List.of(),
                ex,
                ErrorType.CLIENT
        );
    }

    @ExceptionHandler({
            CalendarUnavailableException.class,
            CalendarProviderUnavailableException.class
    })
    public ResponseEntity<ApiErrorResponse> handleUpstreamServiceUnavailable(RuntimeException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                ApiErrorCode.UPSTREAM_SERVICE_UNAVAILABLE,
                ex.getMessage(),
                request,
                List.of(),
                ex,
                ErrorType.SERVER
        );
    }

    @ExceptionHandler({
            PaymentFailedException.class,
            PaymentProviderException.class
    })
    public ResponseEntity<ApiErrorResponse> handleUpstreamServiceError(RuntimeException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_GATEWAY,
                ApiErrorCode.UPSTREAM_SERVICE_ERROR,
                ex.getMessage(),
                request,
                List.of(),
                ex,
                ErrorType.SERVER
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<ApiFieldViolation> violations = extractViolations(ex);
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ApiErrorCode.VALIDATION_ERROR,
                VALIDATION_FAILED_MESSAGE,
                request,
                violations,
                ex,
                ErrorType.CLIENT
        );
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiErrorResponse> handleBindException(BindException ex, HttpServletRequest request) {
        List<ApiFieldViolation> violations = extractViolations(ex);
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ApiErrorCode.VALIDATION_ERROR,
                VALIDATION_FAILED_MESSAGE,
                request,
                violations,
                ex,
                ErrorType.CLIENT
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        List<ApiFieldViolation> violations = ex.getConstraintViolations()
                .stream()
                .map(this::toFieldViolation)
                .toList();
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ApiErrorCode.VALIDATION_ERROR,
                VALIDATION_FAILED_MESSAGE,
                request,
                violations,
                ex,
                ErrorType.CLIENT
        );
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<ApiErrorResponse> handleTransactionSystemException(
            TransactionSystemException ex,
            HttpServletRequest request
    ) {
        ConstraintViolationException validationCause = findCause(ex, ConstraintViolationException.class);
        if (validationCause != null) {
            List<ApiFieldViolation> violations = validationCause.getConstraintViolations()
                    .stream()
                    .map(this::toFieldViolation)
                    .toList();
            return buildErrorResponse(
                    HttpStatus.BAD_REQUEST,
                    ApiErrorCode.VALIDATION_ERROR,
                    VALIDATION_FAILED_MESSAGE,
                    request,
                    violations,
                    validationCause,
                    ErrorType.CLIENT
            );
        }

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ApiErrorCode.INTERNAL_SERVER_ERROR,
                INTERNAL_ERROR_MESSAGE,
                request,
                List.of(),
                ex,
                ErrorType.SERVER
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMalformedBody(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ApiErrorCode.MALFORMED_REQUEST,
                MALFORMED_REQUEST_MESSAGE,
                request,
                List.of(),
                ex,
                ErrorType.CLIENT
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        String requiredType = ex.getRequiredType() == null
                ? "unknown"
                : ex.getRequiredType().getSimpleName();
        String message = String.format(
                "Invalid value '%s' for parameter '%s'. Expected type: %s.",
                stringifyValue(ex.getValue()),
                ex.getName(),
                requiredType
        );

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ApiErrorCode.BAD_REQUEST,
                message,
                request,
                List.of(),
                ex,
                ErrorType.CLIENT
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException ex,
            HttpServletRequest request
    ) {
        String message = "Missing required request parameter '" + ex.getParameterName() + "'.";
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ApiErrorCode.BAD_REQUEST,
                message,
                request,
                List.of(),
                ex,
                ErrorType.CLIENT
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                ApiErrorCode.METHOD_NOT_ALLOWED,
                ex.getMessage(),
                request,
                List.of(),
                ex,
                ErrorType.CLIENT
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ApiErrorCode.INTERNAL_SERVER_ERROR,
                INTERNAL_ERROR_MESSAGE,
                request,
                List.of(),
                ex,
                ErrorType.SERVER
        );
    }

    private ResponseEntity<ApiErrorResponse> buildErrorResponse(
            HttpStatus status,
            ApiErrorCode code,
            String message,
            HttpServletRequest request,
            List<ApiFieldViolation> violations,
            Exception ex,
            ErrorType errorType
    ) {
        String errorId = UUID.randomUUID().toString();
        String path = safePath(request);

        if (errorType == ErrorType.SERVER) {
            log.error(
                    "[{}] {} {} -> {} {} | {}",
                    errorId,
                    request.getMethod(),
                    path,
                    status.value(),
                    code.name(),
                    ex.getMessage(),
                    ex
            );
        } else {
            log.warn(
                    "[{}] {} {} -> {} {} | {}",
                    errorId,
                    request.getMethod(),
                    path,
                    status.value(),
                    code.name(),
                    ex.getMessage()
            );
        }

        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                code.name(),
                message,
                path,
                errorId,
                violations
        );

        return ResponseEntity.status(status).body(body);
    }

    private String safePath(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        return requestUri == null ? "/" : requestUri;
    }

    private List<ApiFieldViolation> extractViolations(BindException ex) {
        List<ApiFieldViolation> violations = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            violations.add(new ApiFieldViolation(
                    fieldError.getField(),
                    defaultMessage(fieldError.getDefaultMessage()),
                    stringifyValue(fieldError.getRejectedValue())
            ));
        }
        ex.getBindingResult().getGlobalErrors().forEach(globalError ->
                violations.add(new ApiFieldViolation(
                        globalError.getObjectName(),
                        defaultMessage(globalError.getDefaultMessage()),
                        null
                ))
        );
        return violations;
    }

    private ApiFieldViolation toFieldViolation(ConstraintViolation<?> violation) {
        return new ApiFieldViolation(
                violation.getPropertyPath().toString(),
                defaultMessage(violation.getMessage()),
                stringifyValue(violation.getInvalidValue())
        );
    }

    private String defaultMessage(String candidate) {
        return candidate == null || candidate.isBlank() ? "Invalid value." : candidate;
    }

    private String stringifyValue(Object value) {
        if (value == null) {
            return null;
        }
        String serialized = String.valueOf(value);
        if (serialized.length() <= MAX_REJECTED_VALUE_LENGTH) {
            return serialized;
        }
        return serialized.substring(0, MAX_REJECTED_VALUE_LENGTH - 3) + "...";
    }

    private <T extends Throwable> T findCause(Throwable throwable, Class<T> targetType) {
        Throwable current = throwable;
        while (current != null) {
            if (targetType.isInstance(current)) {
                return targetType.cast(current);
            }
            current = current.getCause();
        }
        return null;
    }

    private enum ErrorType {
        CLIENT,
        SERVER
    }
}

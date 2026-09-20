package com.example.devicemanagement.exception;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.example.devicemanagement.generated.model.ErrorResponse;
import com.example.devicemanagement.generated.model.ValidationError;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String CODE_DATA_INVALID = "DATA_INVALID";
    private static final String GENERIC_MESSAGE = "An unexpected error occurred";

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        List<ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toValidationError)
                .toList();

        return handleExceptionInternal(ex,
                errorBody(status, "Validation failed", CODE_DATA_INVALID, validationErrors, request),
                headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        String message = ex.getMostSpecificCause() instanceof UnknownDeviceStateException cause
                ? cause.getMessage()
                : "Malformed or unreadable JSON request body";

        return handleExceptionInternal(ex, errorBody(status, message, CODE_DATA_INVALID, null, request),
                headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        return handleExceptionInternal(ex,
                errorBody(status, typeMismatchMessage(ex), CODE_DATA_INVALID, null, request),
                headers, status, request);
    }

    @ExceptionHandler(DeviceNotFoundException.class)
    public ResponseEntity<Object> handleDeviceNotFound(DeviceNotFoundException ex, WebRequest request) {
        return respond(HttpStatus.NOT_FOUND, ex, ex.getMessage(), "DEVICE_NOT_FOUND", request);
    }

    @ExceptionHandler(UnknownDeviceStateException.class)
    public ResponseEntity<Object> handleInvalidState(UnknownDeviceStateException ex, WebRequest request) {
        return respond(HttpStatus.BAD_REQUEST, ex, ex.getMessage(), CODE_DATA_INVALID, request);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Object> handleOptimisticLock(
            ObjectOptimisticLockingFailureException ex, WebRequest request) {

        return respond(HttpStatus.CONFLICT, ex,
                "The device was modified by another request. Re-read it and retry.",
                "OPTIMISTIC_LOCK_CONFLICT", request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrity(DataIntegrityViolationException ex, WebRequest request) {
        return respond(HttpStatus.CONFLICT, ex,
                "The request could not be completed due to a data conflict",
                "DATA_INTEGRITY_VIOLATION", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneric(Exception ex, WebRequest request) {
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, ex, GENERIC_MESSAGE, null, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        logException(ex, status, request);

        Object responseBody = body instanceof ErrorResponse
                ? body
                : errorBody(status, detailOf(ex, status), defaultCode(status), null, request);

        return super.handleExceptionInternal(ex, responseBody, headers, status, request);
    }

    private ResponseEntity<Object> respond(
            HttpStatus status, Exception ex, String message, String code, WebRequest request) {

        String resolvedCode = code != null ? code : defaultCode(status);
        return handleExceptionInternal(ex, errorBody(status, message, resolvedCode, null, request),
                new HttpHeaders(), status, request);
    }

    private ErrorResponse errorBody(HttpStatusCode status, String message, String code,
            List<ValidationError> validationErrors, WebRequest request) {

        return new ErrorResponse()
                .timestamp(OffsetDateTime.now())
                .status(status.value())
                .code(code)
                .message(message)
                .path(pathOf(request))
                .validationErrors(validationErrors);
    }

    private void logException(Exception ex, HttpStatusCode status, WebRequest request) {
        if (status.is5xxServerError()) {
            log.error("Responding {} for {}", status.value(), pathOf(request), ex);
        } else {
            log.warn("Responding {} for {}: {}", status.value(), pathOf(request), ex.getMessage());
        }
    }

    private String detailOf(Exception ex, HttpStatusCode status) {
        if (status.is5xxServerError()) {
            return GENERIC_MESSAGE;
        }
        if (ex instanceof org.springframework.web.ErrorResponse errorResponse
                && errorResponse.getBody()
                .getDetail() != null) {
            return errorResponse.getBody()
                    .getDetail();
        }
        HttpStatus resolved = HttpStatus.resolve(status.value());
        return resolved != null ? resolved.getReasonPhrase() : "Request failed";
    }

    private String typeMismatchMessage(TypeMismatchException ex) {
        if (ex.getMostSpecificCause() instanceof UnknownDeviceStateException cause) {
            return cause.getMessage();
        }
        return ex instanceof MethodArgumentTypeMismatchException mismatch
                ? "Invalid value for parameter '" + mismatch.getName() + "'"
                : "Invalid request parameter";
    }

    private String defaultCode(HttpStatusCode status) {
        if (status.value() == HttpStatus.BAD_REQUEST.value()) {
            return CODE_DATA_INVALID;
        }
        HttpStatus resolved = HttpStatus.resolve(status.value());
        return resolved != null ? resolved.name() : "ERROR";
    }

    private String pathOf(WebRequest request) {
        return request.getDescription(false)
                .replace("uri=", "");
    }

    private ValidationError toValidationError(FieldError fieldError) {
        return new ValidationError().field(fieldError.getField())
                .message(fieldError.getDefaultMessage());
    }
}

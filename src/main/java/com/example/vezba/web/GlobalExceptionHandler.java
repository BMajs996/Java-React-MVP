package com.example.vezba.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors().stream()
            .map(error -> new FieldError(error.getField(), error.getDefaultMessage()))
            .toList();
        return build(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI(), fieldErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> constraintViolation(ConstraintViolationException exception, HttpServletRequest request) {
        List<FieldError> fieldErrors = exception.getConstraintViolations().stream()
            .map(violation -> new FieldError(violation.getPropertyPath().toString(), violation.getMessage()))
            .toList();
        return build(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI(), fieldErrors);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> responseStatus(ResponseStatusException exception, HttpServletRequest request) {
        return build(exception.getStatusCode(), message(exception), request.getRequestURI(), List.of());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingRequestHeaderException.class})
    public ResponseEntity<ApiError> badRequest(Exception exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, message(exception), request.getRequestURI(), List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> unexpected(Exception exception, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", request.getRequestURI(), List.of());
    }

    private ResponseEntity<ApiError> build(HttpStatusCode statusCode, String message, String path, List<FieldError> fieldErrors) {
        int status = statusCode.value();
        HttpStatus httpStatus = HttpStatus.resolve(status);
        String error = httpStatus == null ? "HTTP " + status : httpStatus.getReasonPhrase();
        return ResponseEntity.status(statusCode)
            .body(new ApiError(Instant.now(), status, error, message, path, fieldErrors));
    }

    private String message(ResponseStatusException exception) {
        if (exception.getReason() != null && !exception.getReason().isBlank()) {
            return exception.getReason();
        }
        return message((Exception) exception);
    }

    private String message(Exception exception) {
        if (exception.getMessage() == null || exception.getMessage().isBlank()) {
            return "Request failed";
        }
        return exception.getMessage();
    }

    public record ApiError(Instant timestamp, int status, String error, String message, String path,
                           List<FieldError> fieldErrors) {
    }

    public record FieldError(String field, String message) {
    }
}

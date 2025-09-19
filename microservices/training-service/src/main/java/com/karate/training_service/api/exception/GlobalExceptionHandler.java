package com.karate.training_service.api.exception;

import com.karate.training_service.api.exception.dto.ErrorResponse;
import com.karate.training_service.api.exception.dto.ValidationError;
import com.karate.training_service.domain.exception.*;
import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 - validation errors || invalid JSON / lack of body
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        log.warn("400 Validation failed path={} msg={}", request.getRequestURI(), ex.getMessage());
        List<ValidationError> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> new ValidationError(
                        err.getField(),
                        err.getRejectedValue(),
                        err.getDefaultMessage()
                ))
                .toList();

        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                errors,
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            org.springframework.http.converter.HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        log.warn("400 Malformed body path={} msg={}", request.getRequestURI(), ex.getMessage());
        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Request body is missing or malformed",
                null,
                request.getRequestURI(),
                LocalDateTime.now()
        );
        return ResponseEntity.badRequest().body(response);
    }

    // 400 - invalid business input (duration time)
    @ExceptionHandler(InvalidTrainingTimeRangeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTimeRange(
            InvalidTrainingTimeRangeException ex, HttpServletRequest request) {
        log.warn("400 Invalid training time range path={} msg={}", request.getRequestURI(), ex.getMessage());
        var body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null, request.getRequestURI(), LocalDateTime.now());
        return ResponseEntity.badRequest().body(body);
    }

    // 401 - lack of authentication
    @ExceptionHandler(AuthenticationMissingException.class)
    public ResponseEntity<ErrorResponse> handleAuthMissing(
            AuthenticationMissingException ex, HttpServletRequest request) {
        log.warn("401 Auth missing path={} msg={}", request.getRequestURI(), ex.getMessage());
        var body = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(), ex.getMessage(), null, request.getRequestURI(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    // 403 - forbidden
    @ExceptionHandler(TrainingSessionClubMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTrainingSessionClubMismatch(
            TrainingSessionClubMismatchException ex,
            HttpServletRequest request
    ) {
        log.warn("403 Forbidden path={} msg={}", request.getRequestURI(), ex.getMessage());
        ErrorResponse response = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                ex.getMessage(),
                null,
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // 404 - not found
    @ExceptionHandler({NoSuchElementException.class, EntityNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        log.warn("404 Not found path={} msg={}", request.getRequestURI(), ex.getMessage());
        ErrorResponse response = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                null,
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(TrainingSessionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTrainingSessionNotFound(
            TrainingSessionNotFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("404 Training not found path={} msg={}", request.getRequestURI(), ex.getMessage());
        ErrorResponse response = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                null,
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // 409 - conflict (business logic conflict)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            IllegalStateException ex,
            HttpServletRequest request
    ) {
        log.warn("409 Conflict path={} msg={}", request.getRequestURI(), ex.getMessage());
        ErrorResponse response = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                null,
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // 4xx from Feign (fallback for other client errors)
    @ExceptionHandler(FeignException.FeignClientException.class)
    public ResponseEntity<ErrorResponse> handleFeignClientException(
            FeignException.FeignClientException ex,
            HttpServletRequest request
    ) {
        log.warn("4xx Upstream client error path={} status={} msg={}", request.getRequestURI(), ex.status(), ex.getMessage());
        HttpStatus status = HttpStatus.resolve(ex.status());
        if (status == null) {
            status = HttpStatus.BAD_REQUEST;
        }

        ErrorResponse response = new ErrorResponse(
                status.value(),
                "Upstream service error: " + ex.getMessage(),
                null,
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(status).body(response);
    }

    // 500 - internal server error (fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("500 Unexpected error path={} msg={}", request.getRequestURI(), ex.getMessage(), ex);
        ErrorResponse response = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Unexpected error: " + ex.getMessage(),
                null,
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(UpstreamUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleUpstreamUnavailable(
            UpstreamUnavailableException ex, HttpServletRequest request) {
        log.warn("503 Upstream unavailable path={} msg={}", request.getRequestURI(), ex.getMessage());
        ErrorResponse response = new ErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                ex.getMessage(),
                null,
                request.getRequestURI(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}

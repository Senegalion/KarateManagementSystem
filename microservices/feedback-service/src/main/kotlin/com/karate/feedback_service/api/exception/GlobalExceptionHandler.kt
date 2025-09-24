package com.karate.feedback_service.api.exception;

import com.karate.feedback_service.api.exception.dto.ErrorResponse
import com.karate.feedback_service.api.exception.dto.ValidationError
import com.karate.feedback_service.domain.exception.FeedbackNotFoundException
import com.karate.feedback_service.domain.exception.TrainingSessionNotFoundException
import com.karate.feedback_service.domain.exception.UserNotSignedUpException
import feign.FeignException
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    // 400 - validation errors || invalid JSON / lack of body
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult
            .fieldErrors
            .map { err ->
                ValidationError(
                    field = err.field,
                    rejectedValue = err.rejectedValue,
                    message = err.defaultMessage ?: "Validation error"
                )
            }

        log.warn(
            "Validation failed: {} error(s) at path={} fields={}",
            errors.size, request.requestURI, errors.map { it.field }
        )

        val response = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            message = "Validation failed",
            errors = errors,
            path = request.requestURI,
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity.badRequest().body(response)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.warn(
            "Malformed or missing body at path={} cause={}",
            request.requestURI,
            ex.cause?.javaClass?.simpleName ?: "n/a"
        )

        val response = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            message = "Request body is missing or malformed",
            errors = null,
            path = request.requestURI,
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity.badRequest().body(response)
    }

    // 401 - not authenticated / user missing
    @ExceptionHandler(UsernameNotFoundException::class)
    fun handleUsernameNotFound(
        ex: UsernameNotFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.warn("Unauthorized: {} path={}", ex.message, request.requestURI)

        val response = ErrorResponse(
            status = HttpStatus.UNAUTHORIZED.value(),
            message = ex.message ?: "Unauthorized",
            errors = null,
            path = request.requestURI,
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response)
    }

    // 403 - user not enrolled
    @ExceptionHandler(UserNotSignedUpException::class)
    fun handleUserNotSignedUp(
        ex: UserNotSignedUpException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.warn("Forbidden: {} path={}", ex.message, request.requestURI)

        val response = ErrorResponse(
            status = HttpStatus.FORBIDDEN.value(),
            message = ex.message ?: "Forbidden",
            errors = null,
            path = request.requestURI,
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response)
    }

    // 404 - training session or feedback not found
    @ExceptionHandler(TrainingSessionNotFoundException::class, FeedbackNotFoundException::class)
    fun handleNotFound(
        ex: RuntimeException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.warn("Not found: {} path={}", ex.message, request.requestURI)

        val response = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            message = ex.message ?: "Not found",
            errors = null,
            path = request.requestURI,
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }

    // 409 - conflict (upstream user-service etc.)
    @ExceptionHandler(FeignException.Conflict::class)
    fun handleFeignConflict(
        ex: FeignException.Conflict,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val body = runCatching { ex.contentUTF8() }.getOrNull()
        log.warn(
            "Upstream conflict from Feign: status={} path={} msg={} body={}",
            ex.status(), request.requestURI, ex.message, body
        )

        val response = ErrorResponse(
            status = HttpStatus.CONFLICT.value(),
            message = "Conflict error from upstream service: ${ex.message}",
            errors = null,
            path = request.requestURI,
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response)
    }

    // 415 - unsupported media type
    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleHttpMediaTypeNotSupported(
        ex: HttpMediaTypeNotSupportedException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.warn("Unsupported media type at path={} contentType={}", request.requestURI, ex.contentType)

        val response = ErrorResponse(
            status = HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
            message = "Unsupported media type: ${ex.contentType}",
            errors = null,
            path = request.requestURI,
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(response)
    }

    // 4xx from Feign (fallback for other client errors)
    @ExceptionHandler(FeignException.FeignClientException::class)
    fun handleFeignClientException(
        ex: FeignException.FeignClientException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.resolve(ex.status()) ?: HttpStatus.BAD_REQUEST
        val body = runCatching { ex.contentUTF8() }.getOrNull()
        log.warn(
            "Upstream client error from Feign: status={} path={} msg={} body={}",
            ex.status(), request.requestURI, ex.message, body
        )

        val response = ErrorResponse(
            status = status.value(),
            message = "Upstream service error: ${ex.message}",
            errors = null,
            path = request.requestURI,
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity.status(status).body(response)
    }

    // 500 - internal server error (fallback)
    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.error("Unexpected error at path={} msg={}", request.requestURI, ex.message, ex)

        val response = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            message = "Unexpected error: ${ex.message}",
            errors = null,
            path = request.requestURI,
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }
}

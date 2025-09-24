package com.karate.feedback_service.api.exception.dto;

import java.time.LocalDateTime;

data class ErrorResponse(
    val status: Int,
    val message: String,
    val errors: List<ValidationError>?,
    val path: String,
    val timestamp: LocalDateTime
)

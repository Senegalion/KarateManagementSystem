package com.karate.feedback_service.api.exception.dto;

data class ValidationError(
    val field: String,
    val rejectedValue: Any?,
    val message: String
)

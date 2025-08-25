package com.karate.enrollment_service.api.exception.dto;

public record ValidationError(
        String field,
        Object rejectedValue,
        String message
) {
}

package com.karate.authservice.api.exception.dto;

public record ValidationError(
        String field,
        Object rejectedValue,
        String message
) {
}

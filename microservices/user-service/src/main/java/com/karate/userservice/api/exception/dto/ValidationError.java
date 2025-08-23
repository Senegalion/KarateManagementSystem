package com.karate.userservice.api.exception.dto;

public record ValidationError(
        String field,
        Object rejectedValue,
        String message
) {
}

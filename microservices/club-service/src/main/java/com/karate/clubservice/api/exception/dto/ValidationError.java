package com.karate.clubservice.api.exception.dto;

public record ValidationError(
        String field,
        Object rejectedValue,
        String message
) {
}

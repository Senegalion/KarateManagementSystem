package com.karate.enrollment_service.infrastructure.client.dto;

public record UserPayload(
        Long userId,
        String userEmail,
        String username
) {
}

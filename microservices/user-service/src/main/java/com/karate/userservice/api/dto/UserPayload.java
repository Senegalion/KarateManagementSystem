package com.karate.userservice.api.dto;

public record UserPayload(
        Long userId,
        String userEmail,
        String username
) {
}

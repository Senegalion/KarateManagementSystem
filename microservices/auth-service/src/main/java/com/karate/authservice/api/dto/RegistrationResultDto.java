package com.karate.authservice.api.dto;

import lombok.Builder;

@Builder
public record RegistrationResultDto(
        Long userId,
        String username,
        String email
) {
}

package com.karate.management.karatemanagementsystem.user.api.dto;

import lombok.Builder;

@Builder
public record RegistrationResultDto(
        Long userId,
        String username,
        String email
) {
}

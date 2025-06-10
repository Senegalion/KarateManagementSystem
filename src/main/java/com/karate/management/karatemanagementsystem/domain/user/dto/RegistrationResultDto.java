package com.karate.management.karatemanagementsystem.domain.user.dto;

import lombok.Builder;

@Builder
public record RegistrationResultDto(
        Long userId,
        String username,
        String email
) {
}

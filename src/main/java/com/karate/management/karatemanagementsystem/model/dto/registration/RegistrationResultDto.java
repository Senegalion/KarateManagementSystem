package com.karate.management.karatemanagementsystem.model.dto.registration;

import lombok.Builder;

@Builder
public record RegistrationResultDto(
        Long userId,
        String username,
        String email
) {
}

package com.karate.management.karatemanagementsystem.model.dto;

import lombok.Builder;

@Builder
public record RegistrationResultDto(
        Long userId,
        String username
) {
}

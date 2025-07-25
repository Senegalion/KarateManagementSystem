package com.karate.management.karatemanagementsystem.training.api.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TrainingSessionRegistrationResponseDto(
        String message,
        LocalDateTime date,
        String description
) {
}

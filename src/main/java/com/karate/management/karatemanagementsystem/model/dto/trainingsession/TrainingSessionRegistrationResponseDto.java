package com.karate.management.karatemanagementsystem.model.dto.trainingsession;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TrainingSessionRegistrationResponseDto(
        String message,
        LocalDateTime date,
        String description
) {
}

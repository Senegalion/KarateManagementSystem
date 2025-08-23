package com.karate.training_service.api.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TrainingSessionRegistrationResponseDto(
        String message,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String description
) {
}

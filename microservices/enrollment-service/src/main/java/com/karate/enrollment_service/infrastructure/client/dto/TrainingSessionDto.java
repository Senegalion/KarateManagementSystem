package com.karate.enrollment_service.infrastructure.client.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TrainingSessionDto(
        Long trainingSessionId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String description
) {
}

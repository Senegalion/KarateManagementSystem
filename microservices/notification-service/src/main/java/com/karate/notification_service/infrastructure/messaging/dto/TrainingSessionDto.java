package com.karate.notification_service.infrastructure.messaging.dto;

import java.time.LocalDateTime;

public record TrainingSessionDto(
        Long trainingSessionId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String description
) {
}

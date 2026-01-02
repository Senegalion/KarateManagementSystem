package com.karate.notification_service.infrastructure.messaging.dto;

import java.time.Instant;
import java.time.LocalDateTime;

public record TrainingCreatedEvent(
        String eventId,
        String eventType,
        Instant timestamp,
        Long trainingSessionId,
        Long clubId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String description
) {
}

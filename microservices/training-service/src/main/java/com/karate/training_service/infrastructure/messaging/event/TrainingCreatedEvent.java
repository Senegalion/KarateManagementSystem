package com.karate.training_service.infrastructure.messaging.event;

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
) {}

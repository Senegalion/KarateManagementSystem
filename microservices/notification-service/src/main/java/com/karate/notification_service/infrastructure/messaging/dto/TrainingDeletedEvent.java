package com.karate.notification_service.infrastructure.messaging.dto;

import java.time.Instant;

public record TrainingDeletedEvent(
        String eventId,
        String eventType,
        Instant timestamp,
        Long trainingId,
        Long clubId
) {}

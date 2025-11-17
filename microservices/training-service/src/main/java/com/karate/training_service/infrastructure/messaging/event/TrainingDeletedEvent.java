package com.karate.training_service.infrastructure.messaging.event;

import java.time.Instant;

public record TrainingDeletedEvent(
        String eventId,
        String eventType,
        Instant timestamp,
        Long trainingId,
        Long clubId
) {
}

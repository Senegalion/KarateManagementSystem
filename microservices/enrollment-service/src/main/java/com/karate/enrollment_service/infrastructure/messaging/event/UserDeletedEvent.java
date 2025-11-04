package com.karate.enrollment_service.infrastructure.messaging.event;

import java.time.Instant;

public record UserDeletedEvent(
        String eventId,
        String eventType,
        Instant timestamp,
        Long userId
) {
}

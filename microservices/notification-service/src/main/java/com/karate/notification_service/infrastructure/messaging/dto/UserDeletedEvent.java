package com.karate.notification_service.infrastructure.messaging.dto;

import java.time.Instant;

public record UserDeletedEvent(
        String eventId,
        String eventType,
        Instant timestamp,
        Long userId,
        String email,
        String username
) {
}

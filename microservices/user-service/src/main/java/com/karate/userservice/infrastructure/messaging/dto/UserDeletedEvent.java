package com.karate.userservice.infrastructure.messaging.dto;

import java.time.Instant;

public record UserDeletedEvent(
        String eventId,
        String eventType,
        Instant timestamp,
        Long userId
) {
}

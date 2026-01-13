package com.karate.payment_service.infrastructure.messaging.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Builder
public record PaymentRecordedEvent(
        String eventId,
        String eventType,
        Instant timestamp,
        Long userId,
        String email,
        String username,
        String currency,
        BigDecimal amount,
        List<String> months
) {
}

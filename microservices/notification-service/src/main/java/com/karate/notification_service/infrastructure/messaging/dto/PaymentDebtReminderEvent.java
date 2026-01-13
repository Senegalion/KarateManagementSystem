package com.karate.notification_service.infrastructure.messaging.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PaymentDebtReminderEvent(
        String eventId,
        String eventType,
        Instant timestamp,
        Long userId,
        String email,
        BigDecimal monthlyFee,
        BigDecimal total,
        List<String> months
) {}

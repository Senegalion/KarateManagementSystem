package com.karate.payment_service.api.dto;

import com.karate.payment_service.domain.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PaymentHistoryItemDto(
        Long paymentId,
        String provider,
        String providerOrderId,
        String currency,
        BigDecimal amount,
        PaymentStatus status,
        Instant createdAt,
        Instant paidAt,
        List<String> months
) {
}

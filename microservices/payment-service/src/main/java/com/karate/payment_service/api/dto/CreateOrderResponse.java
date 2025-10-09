package com.karate.payment_service.api.dto;

import com.karate.payment_service.domain.model.PaymentStatus;

import java.math.BigDecimal;
import java.util.List;

public record CreateOrderResponse(
        String providerOrderId,
        String approvalUrl,
        BigDecimal amount,
        String currency,
        List<String> months,
        PaymentStatus status
) {
}

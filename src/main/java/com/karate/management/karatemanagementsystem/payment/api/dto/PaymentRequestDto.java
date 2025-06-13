package com.karate.management.karatemanagementsystem.payment.api.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record PaymentRequestDto(
        Long userId,
        String currencyCode,
        BigDecimal value,
        String returnUrl,
        String cancelUrl
) {
}

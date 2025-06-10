package com.karate.management.karatemanagementsystem.domain.payment.dto;

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

package com.karate.payment_service.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.YearMonth;
import java.util.List;

public record CreateOrderRequest(
        @NotNull
        Long userId,
        @NotEmpty
        List<YearMonth> months,
        String currency,
        String returnUrl,
        String cancelUrl
) {
}

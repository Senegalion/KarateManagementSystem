package com.karate.payment_service.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.YearMonth;
import java.util.List;

@Schema(description = "Payload for creating a new payment order.")
public record CreateOrderRequest(
        @NotNull
        @Schema(description = "User identifier.", example = "42")
        Long userId,
        @NotEmpty
        @Schema(description = "List of months to pay in YYYY-MM.", example = "[\"2025-01\", \"2025-02\"]")
        List<YearMonth> months,
        @Schema(description = "Currency code.", example = "PLN")
        String currency,
        @Schema(description = "URL user is redirected to after success.", example = "http://localhost/success")
        String returnUrl,
        @Schema(description = "URL user is redirected to if they cancel.", example = "http://localhost/cancel")
        String cancelUrl
) {
}

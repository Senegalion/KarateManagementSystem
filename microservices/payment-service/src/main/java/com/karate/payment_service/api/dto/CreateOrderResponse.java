package com.karate.payment_service.api.dto;

import com.karate.payment_service.domain.model.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Payment provider response for a created order.")
public record CreateOrderResponse(

        @Schema(description = "Payment provider order ID.", example = "PAYPAL-XYZ")
        String providerOrderId,

        @Schema(description = "URL for user approval.", example = "https://paypal.com/approval?id=123")
        String approvalUrl,

        @Schema(description = "Total amount for all months.", example = "120.00")
        BigDecimal amount,

        @Schema(description = "Currency code.", example = "PLN")
        String currency,

        @Schema(description = "List of months included in the order.", example = "[\"2025-01\"]")
        List<String> months,

        @Schema(description = "Current status.", example = "CREATED")
        PaymentStatus status
) {
}

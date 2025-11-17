package com.karate.payment_service.api.dto;

import com.karate.payment_service.domain.model.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Schema(description = "Represents a single payment event.")
public record PaymentHistoryItemDto(

        @Schema(description = "Payment identifier.", example = "44")
        Long paymentId,

        @Schema(description = "Payment provider.", example = "PayPal")
        String provider,

        @Schema(description = "Provider order ID.", example = "PAYPAL-123456")
        String providerOrderId,

        @Schema(description = "Currency code.", example = "PLN")
        String currency,

        @Schema(description = "Amount paid.", example = "60.00")
        BigDecimal amount,

        @Schema(description = "Payment status.", example = "COMPLETED")
        PaymentStatus status,

        @Schema(description = "Creation timestamp.", example = "2025-01-12T10:15:00Z")
        Instant createdAt,

        @Schema(description = "Paid timestamp.", example = "2025-01-12T10:17:00Z")
        Instant paidAt,

        @Schema(description = "Months covered by this payment.", example = "[\"2025-02\"]")
        List<String> months
) {
}

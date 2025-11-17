package com.karate.payment_service.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response returned after capturing a payment.")
public record CaptureResponse(

        @Schema(description = "External provider order id.", example = "PAYPAL-12345")
        String providerOrderId,

        @Schema(description = "Status of captured payment.", example = "COMPLETED")
        String status
) {
}

package com.karate.payment_service.api.dto;

public record CaptureResponse(
        String providerOrderId,
        String status) {
}

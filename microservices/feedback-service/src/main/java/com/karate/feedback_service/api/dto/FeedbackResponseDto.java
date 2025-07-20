package com.karate.feedback_service.api.dto;

public record FeedbackResponseDto(
        String comment,
        Integer starRating
) {
}

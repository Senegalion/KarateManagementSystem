package com.karate.management.karatemanagementsystem.feedback.api.dto;

public record FeedbackResponseDto(
        String comment,
        Integer starRating
) {
}

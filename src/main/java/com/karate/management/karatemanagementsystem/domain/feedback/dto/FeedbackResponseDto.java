package com.karate.management.karatemanagementsystem.domain.feedback.dto;

public record FeedbackResponseDto(
        String comment,
        Integer starRating
) {
}

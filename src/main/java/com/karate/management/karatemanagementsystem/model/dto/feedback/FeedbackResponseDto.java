package com.karate.management.karatemanagementsystem.model.dto.feedback;

public record FeedbackResponseDto(
        String comment,
        Integer starRating
) {
}

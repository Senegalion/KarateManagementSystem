package com.karate.management.karatemanagementsystem.domain.feedback.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record FeedbackRequestDto(
        @NotEmpty(message = "{comment.not.empty}")
        @NotNull(message = "{comment.not.null}")
        String comment,
        @Min(value = 1, message = "{starRating.min}")
        @Max(value = 5, message = "{starRating.max}")
        @NotNull(message = "{starRating.not.null}")
        Integer starRating
) {
}

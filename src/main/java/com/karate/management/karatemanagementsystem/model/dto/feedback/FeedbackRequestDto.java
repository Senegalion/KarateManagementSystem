package com.karate.management.karatemanagementsystem.model.dto.feedback;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record FeedbackRequestDto(
        @NotEmpty(message = "{comment.not.empty}")
        @NotNull(message = "{comment.not.null}")
        String comment,
        @NotEmpty(message = "{starRating.not.empty}")
        @NotNull(message = "{starRating.not.null}")
        Integer starRating
) {
}

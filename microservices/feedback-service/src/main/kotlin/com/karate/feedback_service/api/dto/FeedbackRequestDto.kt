package com.karate.feedback_service.api.dto;

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class FeedbackRequestDto(
        @field:NotBlank(message = "{comment.not.blank}")
        val comment: String,
        @field:Min(value = 1, message = "{starRating.min}")
        @field:Max(value = 5, message = "{starRating.max}")
        @field:NotNull(message = "{starRating.not.null}")
        val starRating: Int
)

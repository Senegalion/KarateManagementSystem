package com.karate.feedback_service.api.dto;

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

@Schema(description = "Payload for submitting feedback.")
data class FeedbackRequestDto(
    @field:NotBlank(message = "{comment.not.blank}")
    @Schema(description = "User comment.", example = "Great training!")
    val comment: String,
    @field:Min(value = 1, message = "{starRating.min}")
    @field:Max(value = 5, message = "{starRating.max}")
    @field:NotNull(message = "{starRating.not.null}")
    @Schema(description = "Star rating (1–5).", example = "5")
    val starRating: Int
)

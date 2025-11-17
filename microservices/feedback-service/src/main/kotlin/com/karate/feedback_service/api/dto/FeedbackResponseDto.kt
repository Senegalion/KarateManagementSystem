package com.karate.feedback_service.api.dto;

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Response containing feedback details.")
data class FeedbackResponseDto(
    @Schema(description = "User comment.", example = "Great session!")
    val comment: String,
    @Schema(description = "Star rating.", example = "5")
    val starRating: Int
)

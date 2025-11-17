package com.karate.feedback_service.api.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Extended feedback information for admins.")
data class FeedbackResponseDtoExt(
    @Schema(description = "Feedback identifier.", example = "300")
    val feedbackId: Long,
    @Schema(description = "User identifier.", example = "42")
    val userId: Long,
    @Schema(description = "Training session identifier.", example = "120")
    val trainingSessionId: Long,
    @Schema(description = "Comment.", example = "Very helpful training.")
    val comment: String,
    @Schema(description = "Star rating.", example = "4")
    val starRating: Int
)
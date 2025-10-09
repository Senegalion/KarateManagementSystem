package com.karate.feedback_service.api.dto

data class FeedbackResponseDtoExt(
    val feedbackId: Long,
    val userId: Long,
    val trainingSessionId: Long,
    val comment: String,
    val starRating: Int
)
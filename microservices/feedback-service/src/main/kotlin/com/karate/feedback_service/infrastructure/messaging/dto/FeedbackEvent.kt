package com.karate.feedback_service.infrastructure.messaging.dto;

import java.time.Instant

data class FeedbackEvent(
    val eventId: String,
    val eventType: String,
    val timestamp: Instant,
    val payload: Payload
) {
    data class Payload(
        val userId: Long,
        val userEmail: String,
        val username: String,
        val feedbackText: String
    )
}

package com.karate.feedback_service.infrastructure.messaging.dto

import java.time.Instant

data class UserDeletedEvent(
    val eventId: String,
    val eventType: String,
    val timestamp: Instant,
    val userId: Long
)

package com.karate.userservice.infrastructure.messaging.dto

import java.time.Instant

data class UserDeletedEvent(
    val eventId: String,
    val eventType: String,
    val timestamp: Instant,
    val userId: Long,
    val email: String,
    val username: String
) {
}
package com.karate.feedback_service.domain.model;

import jakarta.persistence.*

@Entity
@Table(name = "feedbacks")
data class FeedbackEntity(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "feedback_id")
        val feedbackId: Long? = null,

        @Column(name = "user_id", nullable = false)
        val userId: Long,

        @Column(name = "training_session_id", nullable = false)
        val trainingSessionId: Long,

        @Column(name = "comment", nullable = false)
        val comment: String,

        @Column(name = "star_rating", nullable = false)
        val starRating: Int
)

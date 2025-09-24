package com.karate.feedback_service.it.config

import com.karate.feedback_service.domain.model.FeedbackEntity

object TestData {
    fun feedback(
        userId: Long = 1,
        trainingId: Long = 2,
        comment: String = "ok",
        stars: Int = 4
    ): FeedbackEntity =
        FeedbackEntity(
            userId = userId,
            trainingSessionId = trainingId,
            comment = comment,
            starRating = stars
        )
}

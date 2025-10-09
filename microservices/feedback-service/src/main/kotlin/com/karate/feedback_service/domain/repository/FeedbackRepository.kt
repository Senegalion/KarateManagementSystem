package com.karate.feedback_service.domain.repository;

import com.karate.feedback_service.domain.model.FeedbackEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FeedbackRepository : JpaRepository<FeedbackEntity, Long> {
    fun findByUserIdAndTrainingSessionId(userId: Long, trainingSessionId: Long): Optional<FeedbackEntity>
    fun findAllByUserId(userId: Long): List<FeedbackEntity>
    fun findAllByTrainingSessionId(trainingSessionId: Long): List<FeedbackEntity>
}

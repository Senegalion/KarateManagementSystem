package com.karate.feedback_service.infrastructure.messaging.dto

import com.karate.feedback_service.domain.repository.FeedbackRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class UserDeletedListener(
    private val feedbackRepository: FeedbackRepository
) {

    private val log = LoggerFactory.getLogger(UserDeletedListener::class.java)

    @Transactional
    @KafkaListener(topics = ["user.deleted"], groupId = "feedback-service-user-deleted")
    fun onUserDeleted(evt: UserDeletedEvent) {
        val count = feedbackRepository.deleteAllByUserId(evt.userId)
        log.info("feedback-service: handled USER_DELETED userId={} deleted={} feedback(s)", evt.userId, count)
    }
}
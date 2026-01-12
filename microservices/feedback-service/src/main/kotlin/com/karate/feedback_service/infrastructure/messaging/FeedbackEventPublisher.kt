package com.karate.feedback_service.infrastructure.messaging;

import com.karate.feedback_service.infrastructure.client.AuthClient
import com.karate.feedback_service.infrastructure.client.UserClient
import com.karate.feedback_service.infrastructure.messaging.dto.FeedbackEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

@Component
class FeedbackEventPublisher(
    private val kafka: KafkaTemplate<String, FeedbackEvent>,
    private val userClient: UserClient,
    private val authClient: AuthClient,
    @Value("\${topics.feedback-created}") private val topicCreated: String,
    @Value("\${topics.feedback-updated}") private val topicUpdated: String,
) {
    private val log = LoggerFactory.getLogger(FeedbackEventPublisher::class.java)

    fun publishCreated(userId: Long, feedbackText: String) {
        val email = userClient.getUserById(userId).email
        val username = authClient.getUsernameById(userId)

        val ev = FeedbackEvent(
            eventId = UUID.randomUUID().toString(),
            eventType = "FEEDBACK_CREATED",
            timestamp = Instant.now(),
            payload = FeedbackEvent.Payload(
                userId = userId,
                userEmail = email,
                username = username!!,
                feedbackText = feedbackText
            )
        )

        kafka.send(topicCreated, userId.toString(), ev)
            .whenComplete { res, ex ->
                if (ex != null) log.error("FEEDBACK_CREATED send FAILED topic={} userId={}", topicCreated, userId, ex)
                else log.info("FEEDBACK_CREATED sent topic={} partition={} offset={} userId={}",
                    res.recordMetadata.topic(), res.recordMetadata.partition(), res.recordMetadata.offset(), userId)
            }
    }

    fun publishUpdated(userId: Long, feedbackText: String) {
        val email = userClient.getUserById(userId).email
        val username = authClient.getUsernameById(userId)

        val ev = FeedbackEvent(
            eventId = UUID.randomUUID().toString(),
            eventType = "FEEDBACK_UPDATED",
            timestamp = Instant.now(),
            payload = FeedbackEvent.Payload(
                userId = userId,
                userEmail = email,
                username = username!!,
                feedbackText = feedbackText
            )
        )

        kafka.send(topicUpdated, userId.toString(), ev)
            .whenComplete { res, ex ->
                if (ex != null) log.error("FEEDBACK_UPDATED send FAILED topic={} userId={}", topicUpdated, userId, ex)
                else log.info("FEEDBACK_UPDATED sent topic={} partition={} offset={} userId={}",
                    res.recordMetadata.topic(), res.recordMetadata.partition(), res.recordMetadata.offset(), userId)
            }
    }
}

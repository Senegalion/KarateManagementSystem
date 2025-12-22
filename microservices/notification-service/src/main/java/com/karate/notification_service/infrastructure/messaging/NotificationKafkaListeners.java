package com.karate.notification_service.infrastructure.messaging;

import com.karate.notification_service.domain.NotificationService;
import com.karate.notification_service.infrastructure.messaging.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationKafkaListeners {

    private final NotificationService notifications;

    @KafkaListener(
            topics = "${topics.user-registered}",
            groupId = "notification-service",
            containerFactory = "userRegisteredListenerFactory"
    )
    public void onUserRegistered(UserRegisteredEvent event) {
        log.info("UserRegistered consumed id={} type={} ts={}",
                event.getEventId(), event.getEventType(), event.getTimestamp());
        notifications.onUserRegistered(event);
    }

    @KafkaListener(
            topics = "${topics.enrollment-created}",
            groupId = "notification-service",
            containerFactory = "enrollmentListenerFactory"
    )
    public void onEnrollment(EnrollmentEvent event) {
        log.info("Enrollment consumed id={} type={} ts={}",
                event.getEventId(), event.getEventType(), event.getTimestamp());
        notifications.onEnrollmentCreated(event);
    }

    @KafkaListener(
            topics = "${topics.feedback-created}",
            groupId = "notification-service",
            containerFactory = "feedbackListenerFactory"
    )
    public void onFeedback(FeedbackEvent event) {
        log.info("Feedback consumed id={} type={} ts={}",
                event.getEventId(), event.getEventType(), event.getTimestamp());
        notifications.onFeedbackCreated(event);
    }

    @KafkaListener(
            topics = "${topics.training-created}",
            groupId = "notification-service",
            containerFactory = "trainingCreatedListenerFactory"
    )
    public void onTrainingCreated(TrainingCreatedEvent event) {
        try {
            log.info("TrainingCreated consumed eventId={} clubId={} trainingSessionId={} startTime={}",
                    event.eventId(), event.clubId(), event.trainingSessionId(), event.startTime());
            notifications.onTrainingCreated(event);
            log.info("TrainingCreated handled OK eventId={}", event.eventId());
        } catch (Exception e) {
            log.error("TrainingCreated FAILED eventId={} clubId={} trainingSessionId={}",
                    event.eventId(), event.clubId(), event.trainingSessionId(), e);
            throw e;
        }
    }

    @KafkaListener(
            topics = "${topics.training-deleted}",
            groupId = "notification-service",
            containerFactory = "trainingDeletedListenerFactory"
    )
    public void onTrainingDeleted(TrainingDeletedEvent event) {
        log.info("TrainingDeleted consumed eventId={} clubId={} trainingId={}",
                event.eventId(), event.clubId(), event.trainingId());
        notifications.onTrainingDeleted(event);
    }

}

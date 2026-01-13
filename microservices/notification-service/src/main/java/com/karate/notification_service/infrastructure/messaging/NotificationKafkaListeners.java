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

    @KafkaListener(
            topics = "${topics.user-deleted}",
            groupId = "notification-service",
            containerFactory = "userDeletedListenerFactory"
    )
    public void onUserDeleted(UserDeletedEvent event) {
        log.info("UserDeleted consumed eventId={} userId={} email={}",
                event.eventId(), event.userId(), event.email());
        notifications.onUserDeleted(event);
    }

    @KafkaListener(
            topics = "${topics.feedback-created}",
            groupId = "notification-service",
            containerFactory = "feedbackListenerFactory"
    )
    public void onFeedbackCreated(FeedbackEvent event) {
        log.info("Feedback CREATED consumed eventId={} userId={} email={}",
                event.getEventId(),
                event.getPayload() == null ? null : event.getPayload().getUserId(),
                event.getPayload() == null ? null : event.getPayload().getUserEmail());
        notifications.onFeedbackCreated(event);
    }

    @KafkaListener(
            topics = "${topics.feedback-updated}",
            groupId = "notification-service",
            containerFactory = "feedbackListenerFactory"
    )
    public void onFeedbackUpdated(FeedbackEvent event) {
        log.info("Feedback UPDATED consumed eventId={} userId={} email={}",
                event.getEventId(),
                event.getPayload() == null ? null : event.getPayload().getUserId(),
                event.getPayload() == null ? null : event.getPayload().getUserEmail());
        notifications.onFeedbackUpdated(event);
    }

    @KafkaListener(
            topics = "${topics.payment-recorded}",
            groupId = "notification-service-payment-recorded",
            containerFactory = "paymentRecordedListenerFactory"
    )
    public void onPaymentRecorded(PaymentRecordedEvent ev) {
        log.info("Payment RECORDED consumed eventId={} userId={} email={}", ev.eventId(), ev.userId(), ev.email());
        notifications.onPaymentRecorded(ev);
    }

    @KafkaListener(
            topics = "${topics.payment-debt-reminder}",
            groupId = "notification-service-payment-debt-reminder",
            containerFactory = "paymentDebtReminderListenerFactory"
    )
    public void onPaymentDebtReminder(PaymentDebtReminderEvent ev) {
        log.info("Payment DEBT REMINDER consumed eventId={} userId={} email={}", ev.eventId(), ev.userId(), ev.email());
        notifications.onPaymentDebtReminder(ev);
    }
}

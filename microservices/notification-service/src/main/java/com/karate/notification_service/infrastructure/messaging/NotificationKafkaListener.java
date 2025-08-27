package com.karate.notification_service.infrastructure.messaging;

import com.karate.notification_service.infrastructure.messaging.dto.EnrollmentEvent;
import com.karate.notification_service.infrastructure.messaging.dto.UserRegisteredEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationKafkaListener {

    @KafkaListener(
            topics = "enrollments",
            groupId = "notification-service",
            containerFactory = "enrollmentKafkaListenerContainerFactory"
    )
    public void handleEnrollmentEvent(EnrollmentEvent event) {
        System.out.println("[Enrollment Event] ID: " + event.getEventId());
        System.out.println("[Enrollment Event] Type: " + event.getEventType());
        System.out.println("[Enrollment Event] Timestamp: " + event.getTimestamp());
        System.out.println("[Enrollment Event] Payload: " + event.getPayload());
    }

    @KafkaListener(
            topics = "users",
            groupId = "notification-service",
            containerFactory = "userKafkaListenerContainerFactory"
    )
    public void handleUserRegistrationEvent(UserRegisteredEvent event) {
        System.out.println("[Enrollment Event] ID: " + event.getEventId());
        System.out.println("[Enrollment Event] Type: " + event.getEventType());
        System.out.println("[Enrollment Event] Timestamp: " + event.getTimestamp());
        System.out.println("[Enrollment Event] Payload: " + event.getPayload());
    }
}

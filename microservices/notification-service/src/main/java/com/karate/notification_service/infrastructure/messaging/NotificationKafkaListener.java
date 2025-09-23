package com.karate.notification_service.infrastructure.messaging;

import com.karate.notification_service.infrastructure.messaging.dto.EnrollmentEvent;
import com.karate.notification_service.infrastructure.messaging.dto.UserRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationKafkaListener {

    @KafkaListener(
            topics = "enrollments",
            groupId = "notification-service",
            containerFactory = "enrollmentKafkaListenerContainerFactory"
    )
    public void handleEnrollmentEvent(EnrollmentEvent event) {
        log.info("[Enrollment Event] id={} type={} timestamp={} payload={}",
                event.getEventId(), event.getEventType(), event.getTimestamp(), event.getPayload());
    }

    @KafkaListener(
            topics = "users",
            groupId = "notification-service",
            containerFactory = "userKafkaListenerContainerFactory"
    )
    public void handleUserRegistrationEvent(UserRegisteredEvent event) {
        log.info("[UserRegistered Event] id={} type={} timestamp={} payload={}",
                event.getEventId(), event.getEventType(), event.getTimestamp(), event.getPayload());
    }
}

package com.karate.notification_service.infrastructure.messaging;

import com.karate.notification_service.infrastructure.email.EmailService;
import com.karate.notification_service.infrastructure.messaging.dto.EnrollmentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnrollmentConsumer {

    private final EmailService emailService;

    @KafkaListener(topics = "enrollment-events", groupId = "notification-service", containerFactory = "kafkaListenerContainerFactory")
    public void consume(EnrollmentDto enrollmentDto) {
        emailService.sendEnrollmentNotification(enrollmentDto);
    }
}

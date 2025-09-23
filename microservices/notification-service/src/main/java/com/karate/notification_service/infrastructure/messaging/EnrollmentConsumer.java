package com.karate.notification_service.infrastructure.messaging;

import com.karate.notification_service.infrastructure.email.EmailService;
import com.karate.notification_service.infrastructure.messaging.dto.EnrollmentEvent;
import com.karate.notification_service.infrastructure.messaging.dto.TrainingSessionDto;
import com.karate.notification_service.infrastructure.messaging.dto.UserInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class EnrollmentConsumer {

    private final EmailService emailService;

    @KafkaListener(
            topics = "enrollments",
            groupId = "notification-service",
            containerFactory = "enrollmentKafkaListenerContainerFactory"
    )
    public void consume(EnrollmentEvent event) {
        var p = event.getPayload();

        var user = new UserInfoDto(
                p.getUserId(),
                p.getUserEmail(),
                null,
                null
        );

        var training = new TrainingSessionDto(
                p.getTrainingId(),
                p.getTrainingStart(),
                p.getTrainingEnd(),
                p.getTrainingDescription()
        );

        var dto = new com.karate.notification_service.infrastructure.messaging.dto.EnrollmentDto(
                p.getUserId(),
                user,
                training,
                LocalDateTime.now()
        );

        emailService.sendEnrollmentNotification(dto);
    }
}

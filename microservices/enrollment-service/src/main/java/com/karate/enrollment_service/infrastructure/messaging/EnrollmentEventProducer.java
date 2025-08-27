package com.karate.enrollment_service.infrastructure.messaging;

import com.karate.enrollment_service.infrastructure.messaging.event.EnrollmentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnrollmentEventProducer {

    private final KafkaTemplate<String, EnrollmentEvent> kafkaTemplate;

    public void sendEnrollmentEvent(EnrollmentEvent event) {
        kafkaTemplate.send("enrollments", event.getEventId(), event);
    }
}

package com.karate.enrollment_service.infrastructure.messaging;

import com.karate.enrollment_service.infrastructure.messaging.event.EnrollmentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollmentEventProducer {

    private final KafkaTemplate<String, EnrollmentEvent> kafkaTemplate;

    public void sendEnrollmentEvent(EnrollmentEvent event) {
        log.info("Kafka send topic='enrollments' key={} type={}", event.getEventId(), event.getEventType());
        kafkaTemplate.send("enrollments", event.getEventId(), event);
    }
}

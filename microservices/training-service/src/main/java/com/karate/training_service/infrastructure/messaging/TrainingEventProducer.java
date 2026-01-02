package com.karate.training_service.infrastructure.messaging;

import com.karate.training_service.infrastructure.messaging.event.TrainingCreatedEvent;
import com.karate.training_service.infrastructure.messaging.event.TrainingDeletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingEventProducer {

    @Value("${topics.training-deleted}")
    private String trainingDeletedTopic;

    @Value("${topics.training-created}")
    private String trainingCreatedTopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendTrainingDeletedEvent(TrainingDeletedEvent event) {
        log.info("Kafka send topic='{}' key={} type={}", trainingDeletedTopic, event.eventId(), event.eventType());
        kafkaTemplate.send(trainingDeletedTopic, event.eventId(), event);
    }

    public void sendTrainingCreatedEvent(TrainingCreatedEvent event) {
        String key = String.valueOf(event.trainingSessionId());
        log.info("Kafka send topic='{}' key={} eventId={} type={}",
                trainingCreatedTopic, key, event.eventId(), event.eventType());
        kafkaTemplate.send(trainingCreatedTopic, key, event);
    }
}

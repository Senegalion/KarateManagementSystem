package com.karate.training_service.infrastructure.messaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TrainingEventProducerTest {

    @Mock
    org.springframework.kafka.core.KafkaTemplate<String, Object> kafka;

    TrainingEventProducer producer;

    @BeforeEach
    void setUp() throws Exception {
        producer = new TrainingEventProducer(kafka);

        var f1 = TrainingEventProducer.class.getDeclaredField("trainingDeletedTopic");
        f1.setAccessible(true);
        f1.set(producer, "topic.deleted");

        var f2 = TrainingEventProducer.class.getDeclaredField("trainingCreatedTopic");
        f2.setAccessible(true);
        f2.set(producer, "topic.created");
    }

    @Test
    void sendTrainingDeletedEvent_sendsToKafka() {
        var ev = new com.karate.training_service.infrastructure.messaging.event.TrainingDeletedEvent(
                "eid", "TrainingDeleted", java.time.Instant.now(), 5L, 7L
        );

        producer.sendTrainingDeletedEvent(ev);

        verify(kafka).send("topic.deleted", "eid", ev);
    }

    @Test
    void sendTrainingCreatedEvent_sendsToKafka_withKeyTrainingId() {
        var ev = new com.karate.training_service.infrastructure.messaging.event.TrainingCreatedEvent(
                "eid", "TRAINING_CREATED", java.time.Instant.now(), 5L, 7L,
                java.time.LocalDateTime.now(), java.time.LocalDateTime.now().plusHours(1), "d"
        );

        producer.sendTrainingCreatedEvent(ev);

        verify(kafka).send("topic.created", "5", ev);
    }
}
package com.karate.authservice.infrastructure.messaging;

import com.karate.authservice.infrastructure.messaging.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventProducer {

    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;

    public void sendUserRegisteredEvent(UserRegisteredEvent event) {
        String key = event.getEventId();
        log.info("Kafka send topic=users key={} type={} userId={}", key, event.getEventType(), event.getPayload().getUserId());
        kafkaTemplate.send("users", key, event)
                .whenComplete((res, ex) -> {
                    if (ex != null) {
                        log.error("Kafka send failed topic=users key={} reason={}", key, ex.getMessage(), ex);
                    } else {
                        log.debug("Kafka send ok topic=users key={} partition={} offset={}",
                                key, res.getRecordMetadata().partition(), res.getRecordMetadata().offset());
                    }
                });
    }
}

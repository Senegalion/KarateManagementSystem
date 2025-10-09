package com.karate.authservice.infrastructure.messaging;

import com.karate.authservice.infrastructure.messaging.dto.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventProducer {

    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;

    @Value("${topics.user-registered}")
    private String userRegisteredTopic;

    public void sendUserRegisteredEvent(UserRegisteredEvent event) {
        String key = event.getEventId();
        log.info("Kafka send topic={} key={} type={} userId={}",
                userRegisteredTopic, key, event.getEventType(), event.getPayload().getUserId());

        kafkaTemplate.send(userRegisteredTopic, key, event)
                .whenComplete((res, ex) -> {
                    if (ex != null) {
                        log.error("Kafka send failed topic={} key={} reason={}",
                                userRegisteredTopic, key, ex.getMessage(), ex);
                    } else {
                        log.debug("Kafka send ok topic={} key={} partition={} offset={}",
                                userRegisteredTopic, key, res.getRecordMetadata().partition(), res.getRecordMetadata().offset());
                    }
                });
    }
}

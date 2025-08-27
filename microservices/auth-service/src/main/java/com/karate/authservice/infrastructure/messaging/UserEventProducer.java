package com.karate.authservice.infrastructure.messaging;

import com.karate.authservice.infrastructure.messaging.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserEventProducer {

    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;

    public void sendUserRegisteredEvent(UserRegisteredEvent event) {
        kafkaTemplate.send("users", event.getEventId(), event);
    }
}

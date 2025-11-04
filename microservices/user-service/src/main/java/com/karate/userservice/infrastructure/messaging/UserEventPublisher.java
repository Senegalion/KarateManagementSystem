package com.karate.userservice.infrastructure.messaging;

import com.karate.userservice.infrastructure.messaging.dto.UserDeletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserEventPublisher {

    @Value("${topics.user-deleted}")
    private String userDeletedTopic;
    private final KafkaTemplate<String, UserDeletedEvent> kafka;

    public void publishUserDeleted(Long userId) {
        var evt = new UserDeletedEvent(
                UUID.randomUUID().toString(),
                "USER_DELETED",
                Instant.now(),
                userId
        );
        kafka.send(userDeletedTopic, String.valueOf(userId), evt);
    }
}

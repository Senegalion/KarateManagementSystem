package com.karate.userservice.infrastructure.messaging;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class UserEventPublisherTest {

    @Test
    void publish_user_deleted_sends_event_to_topic_with_key() {
        @SuppressWarnings("unchecked")
        var kafka = (org.springframework.kafka.core.KafkaTemplate<String, com.karate.userservice.infrastructure.messaging.dto.UserDeletedEvent>) mock(org.springframework.kafka.core.KafkaTemplate.class);

        var publisher = new com.karate.userservice.infrastructure.messaging.UserEventPublisher(kafka);

        org.springframework.test.util.ReflectionTestUtils.setField(publisher, "userDeletedTopic", "user-deleted-topic");

        publisher.publishUserDeleted(123L);

        var capt = org.mockito.ArgumentCaptor.forClass(com.karate.userservice.infrastructure.messaging.dto.UserDeletedEvent.class);
        verify(kafka).send(eq("user-deleted-topic"), eq("123"), capt.capture());

        var evt = capt.getValue();
        assertThat(evt.userId()).isEqualTo(123L);
        assertThat(evt.eventType()).isEqualTo("USER_DELETED");
        assertThat(evt.eventId()).isNotBlank();
        assertThat(evt.timestamp()).isNotNull();
    }
}
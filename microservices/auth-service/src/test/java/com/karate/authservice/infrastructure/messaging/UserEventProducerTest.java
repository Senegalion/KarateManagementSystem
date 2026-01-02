package com.karate.authservice.infrastructure.messaging;

import com.karate.authservice.infrastructure.messaging.dto.UserRegisteredEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserEventProducerTest {

    @Mock
    org.springframework.kafka.core.KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;

    @Test
    void sendUserRegisteredEvent_sendsWithKey_eventId() throws Exception {
        var producer = new UserEventProducer(kafkaTemplate);

        var f = UserEventProducer.class.getDeclaredField("userRegisteredTopic");
        f.setAccessible(true);
        f.set(producer, "user-registered");

        var event = new UserRegisteredEvent();
        event.setEventId("e-1");
        event.setEventType("USER_REGISTERED");
        var payload = new UserRegisteredEvent.Payload();
        payload.setUserId(10L);
        event.setPayload(payload);

        var future = new java.util.concurrent.CompletableFuture<org.springframework.kafka.support.SendResult<String, UserRegisteredEvent>>();
        future.completeExceptionally(new RuntimeException("kafka-down"));

        when(kafkaTemplate.send("user-registered", "e-1", event)).thenReturn(future);

        producer.sendUserRegisteredEvent(event);

        verify(kafkaTemplate).send("user-registered", "e-1", event);
    }

    @Test
    void sendUserRegisteredEvent_successfulSend_hitsSuccessBranch() throws Exception {
        var producer = new UserEventProducer(kafkaTemplate);

        var f = UserEventProducer.class.getDeclaredField("userRegisteredTopic");
        f.setAccessible(true);
        f.set(producer, "user-registered");

        var event = new UserRegisteredEvent();
        event.setEventId("e-2");
        event.setEventType("USER_REGISTERED");
        var payload = new UserRegisteredEvent.Payload();
        payload.setUserId(20L);
        event.setPayload(payload);

        var sendResult = mock(org.springframework.kafka.support.SendResult.class);
        var future = new java.util.concurrent.CompletableFuture<
                org.springframework.kafka.support.SendResult<String, UserRegisteredEvent>>();
        future.complete(sendResult);

        when(kafkaTemplate.send("user-registered", "e-2", event)).thenReturn(future);

        producer.sendUserRegisteredEvent(event);

        verify(kafkaTemplate).send("user-registered", "e-2", event);
    }
}
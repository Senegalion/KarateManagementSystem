package com.karate.notification_service.unit.service;

import com.karate.notification_service.infrastructure.email.EmailService;
import com.karate.notification_service.infrastructure.messaging.EnrollmentConsumer;
import com.karate.notification_service.infrastructure.messaging.dto.EnrollmentEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EnrollmentConsumerTest {

    @Test
    void consume_mapsEventAndCallsEmailService() {
        // given
        EmailService emailService = mock(EmailService.class);
        EnrollmentConsumer consumer = new EnrollmentConsumer(emailService);

        var payload = new EnrollmentEvent.Payload(
                123L,
                "john@ex.com",
                "john",
                456L,
                "Evening Karate",
                LocalDateTime.of(2025, 9, 23, 18, 0),
                LocalDateTime.of(2025, 9, 23, 19, 0)
        );
        var event = new EnrollmentEvent("id-1", "ENROLLMENT", Instant.now(), payload);

        // when
        consumer.consume(event);

        // then
        verify(emailService, times(1)).sendEnrollmentNotification(any());
    }
}

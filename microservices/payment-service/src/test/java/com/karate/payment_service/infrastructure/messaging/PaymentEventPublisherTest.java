package com.karate.payment_service.infrastructure.messaging;

import com.karate.payment_service.infrastructure.messaging.dto.PaymentDebtReminderEvent;
import com.karate.payment_service.infrastructure.messaging.dto.PaymentReceivedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PaymentEventPublisherTest {

    @Test
    void publishReceived_sendsToTopic() {
        KafkaTemplate<String, Object> kt = mock(KafkaTemplate.class);
        PaymentEventPublisher pub = new PaymentEventPublisher(kt);
        ReflectionTestUtils.setField(pub, "topic", "payment-topic");

        PaymentReceivedEvent ev = PaymentReceivedEvent.builder().eventId("1").build();
        pub.publishReceived(ev);

        verify(kt).send("payment-topic", ev);
    }

    @Test
    void publishReminder_sendsToTopic() {
        KafkaTemplate<String, Object> kt = mock(KafkaTemplate.class);
        PaymentEventPublisher pub = new PaymentEventPublisher(kt);
        ReflectionTestUtils.setField(pub, "topic", "payment-topic");

        PaymentDebtReminderEvent ev = PaymentDebtReminderEvent.builder().eventId("2").build();
        pub.publishReminder(ev);

        verify(kt).send("payment-topic", ev);
    }
}

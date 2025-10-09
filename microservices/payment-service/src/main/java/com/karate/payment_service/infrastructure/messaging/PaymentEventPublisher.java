package com.karate.payment_service.infrastructure.messaging;

import com.karate.payment_service.infrastructure.messaging.dto.PaymentDebtReminderEvent;
import com.karate.payment_service.infrastructure.messaging.dto.PaymentReceivedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${topics.payment-events}")
    private String topic;

    public void publishReceived(PaymentReceivedEvent ev) {
        kafkaTemplate.send(topic, ev);
    }

    public void publishReminder(PaymentDebtReminderEvent ev) {
        kafkaTemplate.send(topic, ev);
    }
}

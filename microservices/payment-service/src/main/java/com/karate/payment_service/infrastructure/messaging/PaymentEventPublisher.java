package com.karate.payment_service.infrastructure.messaging;

import com.karate.payment_service.infrastructure.messaging.dto.PaymentDebtReminderEvent;
import com.karate.payment_service.infrastructure.messaging.dto.PaymentRecordedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${topics.payment-recorded}")
    private String recordedTopic;

    @Value("${topics.payment-debt-reminder}")
    private String reminderTopic;

    public void publishRecorded(PaymentRecordedEvent ev) {
        kafkaTemplate.send(recordedTopic, ev.userId().toString(), ev);
    }

    public void publishReminder(PaymentDebtReminderEvent ev) {
        kafkaTemplate.send(reminderTopic, ev.userId().toString(), ev);
    }
}

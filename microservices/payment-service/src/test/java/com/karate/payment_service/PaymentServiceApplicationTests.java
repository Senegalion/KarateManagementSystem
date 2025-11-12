package com.karate.payment_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        // ... (Poprzednie właściwości)
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.config.enabled=false",
        "spring.config.import=",

        "spring.cloud.function.definition=",
        "spring.cloud.stream.bindings.input.destination=disabled",
        "spring.cloud.stream.bindings.output.destination=disabled",

        "spring.kafka.bootstrap-servers=localhost:9092",
        "spring.kafka.consumer.auto-startup=false",

        "spring.application.name=payment-service-test",
        "payment.gateway.secret=dummy-secret",
        "payment.gateway.url=http://dummy.url",

        "topics.payment-events=dummy-topic-name",
        "topics.user-deleted=dummy-user-deleted-topic",
        "topics.user-registered=dummy-user-registered-topic",

        // ROZWIĄZANIE: DODANIE WYRAŻENIA CRON
        // Używamy prostego wyrażenia lub opcji wyłączającej, jeśli to możliwe
        "payments.reminder.cron=-" // '-' lub '0 0 1 1 *' (raz w miesiącu)
})
class PaymentServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
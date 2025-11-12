package com.karate.notification_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.bus.enabled=false",
        "eureka.client.enabled=false",
        "spring.cloud.stream.enabled=false",
        "spring.kafka.enabled=false",

        "spring.mail.host=localhost",
        "spring.mail.port=3025",
        "spring.mail.username=defaultUsername",
        "spring.mail.password=defaultPassword",
        "spring.mail.properties.mail.smtp.auth=true",
        "spring.mail.properties.mail.smtp.starttls.enable=false",

        "app.mail.from=no-reply@karate.local",
        "app.mail.fromName=Karate Management System"
})
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(TestOverrides.class)
class NotificationServiceApplicationTests {

    @Test
    void contextLoads() {
        // smoke: ma się tylko podnieść kontekst
    }
}

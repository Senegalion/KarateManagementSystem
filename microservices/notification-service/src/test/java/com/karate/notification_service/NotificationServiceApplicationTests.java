package com.karate.notification_service;

import com.karate.notification_service.infrastructure.feign.UserClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

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
        "app.mail.fromName=Karate Management System",

        "app.web.dashboard-url=http://localhost/dashboard",
        "app.web.preferences-url=http://localhost/preferences",
        "app.web.privacy-url=http://localhost/privacy",
        "app.web.training-url=http://localhost/trainings/{trainingId}",

        "app.userService.baseUrl=http://user-service:8080"
})
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(TestOverrides.class)
class NotificationServiceApplicationTests {

    @Test
    void contextLoads() {
    }

    @MockitoBean
    UserClient userClient;

    @MockitoBean
    org.springframework.cache.CacheManager cacheManager;
}

package com.karate.authservice;

import com.karate.authservice.it.config.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        classes = BaseIntegrationTest.TestApp.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.kafka.listener.auto-startup=false"
        }
)
@ActiveProfiles("test")
class AuthServiceApplicationTests extends BaseIntegrationTest {
    @Test
    void contextLoads() {
    }
}
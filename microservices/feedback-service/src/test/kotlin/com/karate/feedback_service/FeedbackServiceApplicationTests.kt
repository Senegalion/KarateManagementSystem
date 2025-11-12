package com.karate.feedback_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest(
    classes = [FeedbackServiceApplication::class],
    properties = [
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
                "org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration," +
                "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
        "spring.main.lazy-initialization=true",
        "spring.cloud.config.enabled=false",
        "spring.cloud.bus.enabled=false",
        "eureka.client.enabled=false",
        "spring.main.web-application-type=none"
    ]
)
class FeedbackServiceApplicationTests {
    @Test
    fun contextLoads() {
    }
}

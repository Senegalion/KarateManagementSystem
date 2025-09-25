package com.karate.clubservice.it.config;

import com.karate.clubservice.api.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Testcontainers
@Import(GlobalExceptionHandler.class)
public abstract class BaseIntegrationTest {

    @Autowired
    protected WebTestClient webTestClient;
    @Autowired
    JdbcTemplate jdbc;

    private static final PostgreSQLContainer<?> POSTGRES = PostgresTc.getInstance();

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);
        r.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        r.add("spring.jpa.show-sql", () -> "false");

        // turn off cloud infra in tests
        r.add("spring.cloud.config.enabled", () -> "false");
        r.add("spring.cloud.bus.enabled", () -> "false");
        r.add("eureka.client.enabled", () -> "false");
        r.add("spring.rabbitmq.listener.simple.auto-startup", () -> "false");
        r.add("management.tracing.enabled", () -> "false");
        r.add("spring.config.import", () -> "");
        // if you have Flyway in the app, disable or point to test migrations:
        r.add("spring.flyway.enabled", () -> "false");
    }

    @BeforeEach
    void cleanDb() {
        jdbc.execute("TRUNCATE TABLE karate_clubs RESTART IDENTITY CASCADE");
    }
}

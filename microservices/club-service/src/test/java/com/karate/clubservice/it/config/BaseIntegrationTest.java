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
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

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

    // 1. Dodaj kontener Redis
    private static final GenericContainer<?> REDIS =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                    .withExposedPorts(6379)
                    .withReuse(true);

    static {
        if (!REDIS.isRunning()) {
            REDIS.start();
        }
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        // ... (konfiguracja PostgreSQL)
        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);
        r.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        r.add("spring.jpa.show-sql", () -> "false");

        // 2. Dodaj dynamiczne właściwości dla Redis
        r.add("spring.data.redis.host", REDIS::getHost);
        r.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));

        // turn off cloud infra in tests
        r.add("spring.cloud.config.enabled", () -> "false");
        r.add("spring.cloud.bus.enabled", () -> "false");
        r.add("eureka.client.enabled", () -> "false");
        r.add("spring.rabbitmq.listener.simple.auto-startup", () -> "false");
        r.add("management.tracing.enabled", () -> "false");
        r.add("spring.config.import", () -> "");
        r.add("spring.flyway.enabled", () -> "false");
    }

    @BeforeEach
    void cleanDb() {
        jdbc.execute("TRUNCATE TABLE karate_clubs RESTART IDENTITY CASCADE");
    }
}

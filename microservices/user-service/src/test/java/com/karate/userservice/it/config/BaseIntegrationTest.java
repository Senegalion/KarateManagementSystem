package com.karate.userservice.it.config;

import com.karate.userservice.it.config.security.TestSecurityConfig;
import com.karate.userservice.it.config.tc.PostgresTc;
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
@Import(TestSecurityConfig.class)
public abstract class BaseIntegrationTest {

    @Autowired
    JdbcTemplate jdbc;

    private static final PostgreSQLContainer<?> POSTGRES = PostgresTc.getInstance();

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
    static void datasourceProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);
        r.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        r.add("spring.jpa.show-sql", () -> "false");
        r.add("spring.data.redis.host", REDIS::getHost);
        r.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
        r.add("spring.cloud.config.enabled", () -> "false");
        r.add("spring.cloud.bus.enabled", () -> "false");
        r.add("eureka.client.enabled", () -> "false");
        r.add("spring.rabbitmq.host", () -> "localhost");
        r.add("spring.rabbitmq.port", () -> "5672");
        r.add("spring.cloud.bus.enabled", () -> "false");
        r.add("spring.rabbitmq.listener.simple.auto-startup", () -> "false");
        r.add("management.tracing.enabled", () -> "false");
        r.add("spring.config.import", () -> "");
        r.add("spring.kafka.bootstrap-servers", () -> "localhost:9092");
        r.add("spring.kafka.consumer.auto-startup", () -> "false");
    }

    @Autowired
    protected WebTestClient webTestClient;

    @BeforeEach
    void cleanDb() {
        jdbc.execute("TRUNCATE TABLE users, addresses RESTART IDENTITY CASCADE");
    }
}

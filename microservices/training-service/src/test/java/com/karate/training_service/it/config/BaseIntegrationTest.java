package com.karate.training_service.it.config;

import com.karate.training_service.api.exception.GlobalExceptionHandler;
import com.karate.training_service.infrastructure.jwt.JwtAuthTokenFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Testcontainers
@Import({GlobalExceptionHandler.class})
public abstract class BaseIntegrationTest {

    @Autowired
    protected WebTestClient webTestClient;
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

    @MockitoBean
    private JwtAuthTokenFilter jwtAuthTokenFilter;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
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
    }

    @BeforeEach
    void cleanDb() {
        jdbc.execute("TRUNCATE TABLE training_sessions RESTART IDENTITY CASCADE");
    }

    @BeforeEach
    void mockJwt() throws ServletException, IOException {
        Mockito.doAnswer(inv -> {
            var req = (jakarta.servlet.http.HttpServletRequest) inv.getArgument(0);
            var res = (jakarta.servlet.http.HttpServletResponse) inv.getArgument(1);
            var chain = (FilterChain) inv.getArgument(2);

            String user = req.getHeader("X-Test-User");
            String roles = req.getHeader("X-Test-Roles");

            if (user != null) {
                var authorities = roles == null ? java.util.List.<SimpleGrantedAuthority>of()
                        : java.util.Arrays.stream(roles.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isBlank())
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                        .toList();
                var auth = new UsernamePasswordAuthenticationToken(user, "N/A", authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

            chain.doFilter(req, res);
            return null;
        }).when(jwtAuthTokenFilter).doFilter(any(), any(), any());
    }
}

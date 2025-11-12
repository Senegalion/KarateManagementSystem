package com.karate.authservice.it.config;

import com.karate.authservice.api.exception.GlobalExceptionHandler;
import com.karate.authservice.infrastructure.jwt.JwtAuthTokenFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(
        classes = BaseIntegrationTest.TestApp.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.cloud.config.enabled=false",
                "spring.config.import=",
                "spring.cloud.discovery.enabled=false",
                "eureka.client.enabled=false",
                "spring.cloud.bus.enabled=false",
                "management.tracing.enabled=false",

                "spring.kafka.bootstrap-servers=localhost:0",
                "spring.kafka.listener.auto-startup=false",
                "spring.kafka.listener.missing-topics-fatal=false",
                "spring.rabbitmq.host=localhost",
                "spring.rabbitmq.port=0",
                "spring.rabbitmq.listener.simple.auto-startup=false",

                "spring.jpa.open-in-view=false",
                "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
                "spring.flyway.enabled=true",

                "spring.main.allow-bean-definition-overriding=true"
        }
)
@AutoConfigureWebTestClient
@Testcontainers
@Import({GlobalExceptionHandler.class, BaseIntegrationTest.TestSecurityConfig.class})
public abstract class BaseIntegrationTest {

    @SpringBootApplication(
            scanBasePackages = "com.karate.authservice",
            exclude = {
                    KafkaAutoConfiguration.class,
                    RabbitAutoConfiguration.class
            }
    )
    static class TestApp {
    }

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testSecurity(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(reg -> reg.anyRequest().permitAll());
            return http.build();
        }

        @Bean
        PasswordEncoder passwordEncoder() {
            // Użycie NoOpPasswordEncoder tylko w testach
            return NoOpPasswordEncoder.getInstance();
        }
    }

    @Autowired
    protected WebTestClient webTestClient;
    @Autowired
    JdbcTemplate jdbc;

    private static final PostgreSQLContainer<?> POSTGRES = PostgresTc.getInstance();

    @MockitoBean
    private JwtAuthTokenFilter jwtAuthTokenFilter;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);
        r.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        r.add("spring.jpa.show-sql", () -> "false");
        r.add("spring.flyway.enabled", () -> "true");
    }

    @BeforeEach
    void cleanDb() {
        jdbc.execute("TRUNCATE TABLE auth_users_roles RESTART IDENTITY CASCADE");
        jdbc.execute("TRUNCATE TABLE auth_users RESTART IDENTITY CASCADE");
        jdbc.execute("TRUNCATE TABLE roles RESTART IDENTITY CASCADE");
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
                List<SimpleGrantedAuthority> authorities =
                        (roles == null || roles.isBlank())
                                ? List.of()
                                : Arrays.stream(roles.split(","))
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
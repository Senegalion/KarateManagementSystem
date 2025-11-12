package com.karate.authservice;

import com.karate.authservice.domain.service.AuthService;
import com.karate.authservice.infrastructure.jwt.JwtAuthenticatorService;
import com.karate.authservice.infrastructure.messaging.UserDeletedListener;
import com.karate.authservice.infrastructure.messaging.UserEventProducer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(
        properties = {
                "spring.cloud.config.enabled=false",
                "spring.config.import=",
                "spring.cloud.discovery.enabled=false",
                "eureka.client.enabled=false",
                "spring.cloud.bus.enabled=false",
                "management.tracing.enabled=false",

                "spring.main.web-application-type=none",
                "server.port=0",
                "spring.kafka.bootstrap-servers=localhost:0",
                "spring.kafka.listener.auto-startup=false",
                "spring.rabbitmq.host=localhost",
                "spring.rabbitmq.port=0"
        }
)
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        FlywayAutoConfiguration.class,
        SecurityAutoConfiguration.class
})
class AuthServiceApplicationTests {
    @MockBean
    private AuthenticationConfiguration authenticationConfiguration;
    @MockBean
    private AuthService authService;
    @MockBean
    private JwtAuthenticatorService jwtAuthenticatorService;
    @MockBean
    private UserDeletedListener userDeletedListener;
    @MockBean
    private UserEventProducer userEventProducer;
    @MockBean
    private SecurityFilterChain springSecurityFilterChain;

    @Test
    void contextLoads() {
    }
}
package com.karate.feedback_service.it.config

import com.karate.feedback_service.FeedbackServiceApplication
import com.karate.feedback_service.infrastructure.jwt.JwtAuthTokenFilter
import com.karate.feedback_service.it.config.security.TestSecurityConfig
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.junit.jupiter.Testcontainers

@ActiveProfiles("test")
@ExtendWith(SpringExtension::class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "spring.config.import=",
        "spring.main.web-application-type=reactive"
    ],
    classes = [FeedbackServiceApplication::class]
)
@EnableAutoConfiguration(
    exclude = [
        org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration::class,
        org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration::class
    ]
)
@AutoConfigureWebTestClient
@Testcontainers
@Import(TestSecurityConfig::class)
abstract class BaseIntegrationTest {

    companion object {
        private val POSTGRES = PostgresTc.instance

        @JvmStatic
        @DynamicPropertySource
        fun props(r: DynamicPropertyRegistry) {
            r.add("spring.datasource.url", POSTGRES::getJdbcUrl)
            r.add("spring.datasource.username", POSTGRES::getUsername)
            r.add("spring.datasource.password", POSTGRES::getPassword)
            r.add("spring.jpa.hibernate.ddl-auto") { "none" }
            r.add("spring.jpa.show-sql") { "false" }

            r.add("spring.cloud.config.enabled") { "false" }

            r.add("auth.jwt.secretKey") { "test-secret-at-least-32-chars" }
            r.add("auth.jwt.expirationDays") { "1" }
            r.add("auth.jwt.issuer") { "test" }

            r.add("management.tracing.enabled") { "false" }
            r.add("management.prometheus.metrics.export.enabled") { "false" }
        }
    }

    @Autowired
    protected lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var jdbc: JdbcTemplate

    @MockitoBean
    lateinit var jwtAuthTokenFilter: JwtAuthTokenFilter

    @BeforeEach
    fun cleanDb() {
        jdbc.execute("TRUNCATE TABLE feedbacks RESTART IDENTITY CASCADE")
    }
}


package com.karate.management.karatemanagementsystem.infrastructure.api.paypal.controller;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.karate.management.karatemanagementsystem.infrastructure.api.paypal.client.PayPalClientInterface;
import com.karate.management.karatemanagementsystem.infrastructure.api.paypal.service.PayPalService;
import com.karate.management.karatemanagementsystem.domain.payment.dto.PaymentRequestDto;
import com.karate.management.karatemanagementsystem.domain.payment.dto.PaymentResponseDto;
import com.karate.management.karatemanagementsystem.domain.payment.PaymentEntity;
import com.karate.management.karatemanagementsystem.domain.user.UserEntity;
import com.karate.management.karatemanagementsystem.domain.payment.PaymentRepository;
import com.karate.management.karatemanagementsystem.domain.user.UserRepository;
import com.karate.management.karatemanagementsystem.domain.payment.PaymentStatus;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PayPalRESTControllerIT {
    private static final String PAYMENT_REQUEST_PAYLOAD = """
                {
                  "intent": "CAPTURE",
                  "purchase_units": [
                    {
                      "amount": {
                        "currency_code": "USD",
                        "value": "150.00"
                      },
                      "description": "Payment for karate training"
                    }
                  ]
                }
            """.trim();

    private static final String PAYMENT_ID = "6N579553BD4383536";

    @RegisterExtension
    public static WireMockExtension wireMockServer = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15.12"))
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private PayPalService payPalService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @MockBean
    private PayPalClientInterface payPalClient;

    @MockBean
    private Clock clock;

    private UserEntity userEntity;

    @BeforeAll
    void setDbUp() {
        postgres.start();
    }

    @AfterAll
    void tearDbDown() {
        postgres.stop();
    }

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        userRepository.deleteAll();

        userEntity = new UserEntity();
        userEntity.setUsername("testUser");
        userEntity.setEmail("testEmail@gmail.com");
        userEntity.setPassword("password");
        userEntity.setRegistrationDate(LocalDate.now());
        userRepository.save(userEntity);

        Instant fixedInstant = Instant.parse("2025-03-27T00:00:00Z");
        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    void should_create_payment_and_return_payment_response() throws IOException {
        // given
        LocalDate now = LocalDate.now(clock);
        when(payPalClient.createPayment(anyString())).thenReturn(PAYMENT_ID);
        wireMockServer.stubFor(post(urlEqualTo("/v2/checkout/orders"))
                .withRequestBody(equalToJson(PAYMENT_REQUEST_PAYLOAD))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                    {
                                        "id": "6N579553BD4383536",
                                        "userId": 1,
                                        "amount": 150.00,
                                        "status": "PENDING",
                                        "paymentDate": "%s",
                                        "approvalUrl": "https://www.sandbox.paypal.com/checkoutnow?token=6N579553BD4383536"
                                    }
                                """.trim().formatted(now))));

        // when
        PaymentRequestDto paymentRequestDto = new PaymentRequestDto(
                userEntity.getUserId(),
                "USD",
                BigDecimal.valueOf(150.00),
                "http://localhost:8082/success",
                "http://localhost:8082/cancel"
        );
        PaymentResponseDto paymentResponseDto = payPalService.createPayment(paymentRequestDto);

        // then
        assertThat(paymentResponseDto).isNotNull();
        assertThat(paymentResponseDto.paymentId()).isEqualTo("6N579553BD4383536");
        assertThat(paymentResponseDto.userId()).isNotNull();
        assertThat(paymentResponseDto.amount()).isEqualTo(BigDecimal.valueOf(150.00));
        assertThat(paymentResponseDto.status()).isEqualTo(PaymentStatus.PENDING);
        assertThat(paymentResponseDto.paymentDate()).isEqualTo(now.toString());
        assertThat(paymentResponseDto.approvalUrl()).isEqualTo("https://www.sandbox.paypal.com/checkoutnow?token=6N579553BD4383536");
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    void should_throw_exception_when_payment_creation_fails() throws IOException {
        // given
        PaymentRequestDto paymentRequestDto = new PaymentRequestDto(
                userEntity.getUserId(),
                "USD",
                BigDecimal.valueOf(150.00),
                "https://return.url",
                "https://cancel.url"
        );

        when(payPalClient.createPayment(anyString())).thenThrow(new IOException("Internal Server Error"));

        // when
        RuntimeException exception = assertThrows(RuntimeException.class, () -> payPalService.createPayment(paymentRequestDto));

        // then
        assertThat(exception.getMessage()).contains("Error creating payment with PayPal");
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    void should_throw_exception_when_payment_already_confirmed() {
        // given
        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setPaypalOrderId(PAYMENT_ID);
        paymentEntity.setUserEntity(userEntity);
        paymentEntity.setAmount(BigDecimal.valueOf(150));
        paymentEntity.setPaymentStatus(PaymentStatus.PAID);
        paymentEntity.setPaymentDate(LocalDate.now());
        paymentRepository.save(paymentEntity);

        // when
        RuntimeException exception = assertThrows(RuntimeException.class, () -> payPalService.capturePayment(String.valueOf(PAYMENT_ID)));

        // then
        assertThat(exception.getMessage()).contains("Payment already confirmed");
    }
}
package com.karate.management.karatemanagementsystem.infrastructure.api.paypal.controller;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.karate.management.karatemanagementsystem.infrastructure.api.paypal.service.PayPalService;
import com.karate.management.karatemanagementsystem.model.dto.paypal.PaymentResponseDto;
import com.karate.management.karatemanagementsystem.model.entity.UserEntity;
import com.karate.management.karatemanagementsystem.model.repository.PaymentRepository;
import com.karate.management.karatemanagementsystem.model.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PayPalRESTControllerIT {
    public static final String PAYMENT_REQUEST_PAYLOAD = """
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
    public static final Long PAYMENT_ID = 123L;

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
        userEntity = new UserEntity();
        userEntity.setUsername("testUser");
        userEntity.setPassword("password");
        userRepository.save(userEntity);
    }

//    @Test
//    @WithMockUser(username = "testUser", roles = "USER")
//    void should_create_payment_and_return_payment_response() {
//        // given
//        wireMockServer.stubFor(post(urlEqualTo("/v2/checkout/orders"))
//                .withRequestBody(equalToJson(PAYMENT_REQUEST_PAYLOAD))
//                .willReturn(aResponse()
//                        .withStatus(201)
//                        .withBody("{\"id\":\"PAYMENT123\"}"))); // Tu zwracamy id płatności jako odpowiedź
//
//        // when
//        PaymentResponseDto paymentResponseDto = payPalService.createPayment(userEntity.getUserId()); // Odkomentowaliśmy tę linię
//
//        // then
//        assertThat(paymentResponseDto).isNotNull();
//        assertThat(paymentResponseDto.paymentId()).isEqualTo("PAYMENT123");  // Wartość, którą zwróci WireMock
//        assertThat(paymentResponseDto.amount()).isEqualTo(BigDecimal.valueOf(150.00)); // Sprawdzenie kwoty
//    }

//    @Test
//    void should_throw_exception_when_payment_creation_fails() throws IOException {
//        // given
//        wireMockServer.stubFor(post(urlEqualTo("/v2/checkout/orders"))
//                .willReturn(aResponse()
//                        .withStatus(500)
//                        .withBody("{\"error\":\"Internal Server Error\"}")));
//
//        // when
//        Throwable throwable = catchThrowable(() -> payPalService.createPayment(userEntity.getUserId()));
//
//        // then
//        assertThat(throwable).isInstanceOf(RuntimeException.class);
//        assertThat(throwable.getMessage()).contains("Error creating payment");
//    }
//
//    @Test
//    void should_confirm_payment_and_return_payment_response() throws IOException {
//        // given
//        wireMockServer.stubFor(post(urlEqualTo("/v2/checkout/orders/" + PAYMENT_ID + "/capture"))
//                .willReturn(aResponse()
//                        .withStatus(200)
//                        .withBody("{\"status\":\"COMPLETED\"}")));
//
//        // and given payment exists in the database
//        PaymentEntity paymentEntity = new PaymentEntity();
//        paymentEntity.setPaymentId(PAYMENT_ID);
//        paymentEntity.setUserEntity(userEntity);
//        paymentEntity.setAmount(BigDecimal.valueOf(150));
//        paymentEntity.setPaymentStatus(PaymentStatus.PENDING);
//        paymentRepository.save(paymentEntity);
//
//        // when
//        PaymentResponseDto paymentResponseDto = payPalService.confirmPayment(PAYMENT_ID);
//
//        // then
//        assertThat(paymentResponseDto).isNotNull();
//        assertThat(paymentResponseDto.paymentId()).isEqualTo(PAYMENT_ID);
//        assertThat(paymentResponseDto.status()).isEqualTo(PaymentStatus.PAID);
//    }
//
//    @Test
//    void should_throw_exception_when_payment_confirmation_fails() throws IOException {
//        // given
//        wireMockServer.stubFor(post(urlEqualTo("/v2/checkout/orders/" + PAYMENT_ID + "/capture"))
//                .willReturn(aResponse()
//                        .withStatus(500)
//                        .withBody("{\"error\":\"Internal Server Error\"}")));
//
//        // and given payment exists in the database
//        PaymentEntity paymentEntity = new PaymentEntity();
//        paymentEntity.setPaymentId(PAYMENT_ID);
//        paymentEntity.setUserEntity(userEntity);
//        paymentEntity.setAmount(BigDecimal.valueOf(150));
//        paymentEntity.setPaymentStatus(PaymentStatus.PENDING);
//        paymentRepository.save(paymentEntity);
//
//        // when
//        Throwable throwable = catchThrowable(() -> payPalService.confirmPayment(PAYMENT_ID));
//
//        // then
//        assertThat(throwable).isInstanceOf(RuntimeException.class);
//        assertThat(throwable.getMessage()).contains("Payment confirmation failed");
//    }
//
//    @Test
//    void should_throw_exception_when_payment_already_confirmed() throws IOException {
//        // given
//        PaymentEntity paymentEntity = new PaymentEntity();
//        paymentEntity.setPaymentId(PAYMENT_ID);
//        paymentEntity.setUserEntity(userEntity);
//        paymentEntity.setAmount(BigDecimal.valueOf(150));
//        paymentEntity.setPaymentStatus(PaymentStatus.PAID);
//        paymentRepository.save(paymentEntity);
//
//        // when
//        Throwable throwable = catchThrowable(() -> payPalService.confirmPayment(PAYMENT_ID));
//
//        // then
//        assertThat(throwable).isInstanceOf(RuntimeException.class);
//        assertThat(throwable.getMessage()).contains("Payment already confirmed");
//    }
}
package com.karate.management.karatemanagementsystem.infrastructure.api.paypal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karate.management.karatemanagementsystem.infrastructure.api.paypal.client.PayPalClientInterface;
import com.karate.management.karatemanagementsystem.model.dto.paypal.PaymentRequestDto;
import com.karate.management.karatemanagementsystem.model.dto.paypal.PaymentResponseDto;
import com.karate.management.karatemanagementsystem.model.entity.PaymentEntity;
import com.karate.management.karatemanagementsystem.model.entity.UserEntity;
import com.karate.management.karatemanagementsystem.model.repository.PaymentRepository;
import com.karate.management.karatemanagementsystem.model.repository.UserRepository;
import com.karate.management.karatemanagementsystem.model.staticdata.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PayPalServiceTest {
    @Mock
    private PayPalClientInterface payPalClient;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Clock clock;

    @InjectMocks
    private PayPalService payPalService;

    private UserEntity userEntity;
    private PaymentRequestDto paymentRequestDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userEntity = new UserEntity();
        userEntity.setUserId(1L);
        userEntity.setUsername("testUser");

        paymentRequestDto = new PaymentRequestDto(
                1L, "PLN", new BigDecimal("150.00"), "http://returnUrl", "http://cancelUrl"
        );

        // Mocking Authentication and SecurityContextHolder
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("testUser");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(userEntity));

        Instant fixedInstant = Instant.parse("2025-03-27T12:00:00Z");
        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
    }

    @Test
    void should_return_payment_response_when_payment_is_created_successfully() throws Exception {
        // given
        String expectedToken = "fakeToken";
        when(payPalClient.createPayment(anyString())).thenReturn(expectedToken);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // when
        PaymentResponseDto response = payPalService.createPayment(paymentRequestDto);

        // then
        assertNotNull(response);
        assertEquals(expectedToken, response.paymentId());
        assertEquals(userEntity.getUserId(), response.userId());
        assertEquals(new BigDecimal("150.00"), response.amount());
        assertEquals(PaymentStatus.PENDING, response.status());
        assertNotNull(response.approvalUrl());
    }

    @Test
    void should_throw_exception_when_payment_value_is_null() {
        // given
        PaymentRequestDto invalidPaymentRequest = new PaymentRequestDto(
                1L, "PLN", null, "http://returnUrl", "http://cancelUrl"
        );

        // when && then
        assertThrows(IllegalArgumentException.class, () -> payPalService.createPayment(invalidPaymentRequest));
    }

    @Test
    void should_return_payment_response_when_payment_is_captured_successfully() throws Exception {
        // given
        String paypalOrderId = "fakeOrderId";
        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setPaypalOrderId(paypalOrderId);
        paymentEntity.setUserEntity(userEntity);
        paymentEntity.setAmount(new BigDecimal("150.00"));
        paymentEntity.setPaymentStatus(PaymentStatus.PENDING);
        paymentEntity.setPaymentDate(LocalDate.now());

        when(paymentRepository.findByPaypalOrderId(paypalOrderId)).thenReturn(Optional.of(paymentEntity));
        when(payPalClient.capturePayment(paypalOrderId)).thenReturn(true);

        // when
        PaymentResponseDto response = payPalService.capturePayment(paypalOrderId);

        // then
        assertNotNull(response);
        assertEquals(PaymentStatus.PAID, response.status());
    }

    @Test
    void should_throw_exception_when_payment_is_already_paid() {
        // given
        String paypalOrderId = "fakeOrderId";
        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setPaypalOrderId(paypalOrderId);
        paymentEntity.setUserEntity(userEntity);
        paymentEntity.setAmount(new BigDecimal("150.00"));
        paymentEntity.setPaymentStatus(PaymentStatus.PAID);
        paymentEntity.setPaymentDate(LocalDate.now());

        when(paymentRepository.findByPaypalOrderId(paypalOrderId)).thenReturn(Optional.of(paymentEntity));

        // when && then
        assertThrows(RuntimeException.class, () -> payPalService.capturePayment(paypalOrderId));
    }

    @Test
    void should_throw_exception_when_payment_not_found() {
        // given
        String paypalOrderId = "fakeOrderId";
        when(paymentRepository.findByPaypalOrderId(paypalOrderId)).thenReturn(Optional.empty());

        // when && then
        assertThrows(RuntimeException.class, () -> payPalService.capturePayment(paypalOrderId));
    }

    @Test
    void should_throw_exception_when_payment_capture_fails() throws Exception {
        // given
        String paypalOrderId = "fakeOrderId";
        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setPaypalOrderId(paypalOrderId);
        paymentEntity.setUserEntity(userEntity);
        paymentEntity.setAmount(new BigDecimal("150.00"));
        paymentEntity.setPaymentStatus(PaymentStatus.PENDING);
        paymentEntity.setPaymentDate(LocalDate.now());

        when(paymentRepository.findByPaypalOrderId(paypalOrderId)).thenReturn(Optional.of(paymentEntity));
        when(payPalClient.capturePayment(paypalOrderId)).thenReturn(false);

        // when && then
        assertThrows(RuntimeException.class, () -> payPalService.capturePayment(paypalOrderId));
    }
}
//package com.karate.management.karatemanagementsystem.infrastructure.api.paypal.service;
//
//import com.karate.management.karatemanagementsystem.model.dto.paypal.PaymentResponseDto;
//import com.karate.management.karatemanagementsystem.model.entity.PaymentEntity;
//import com.karate.management.karatemanagementsystem.model.entity.UserEntity;
//import com.karate.management.karatemanagementsystem.model.repository.PaymentRepository;
//import com.karate.management.karatemanagementsystem.model.repository.UserRepository;
//import com.karate.management.karatemanagementsystem.model.staticdata.PaymentStatus;
//import com.karate.management.karatemanagementsystem.service.exception.PaymentAlreadyConfirmed;
//import com.karate.management.karatemanagementsystem.service.exception.PaymentNotFoundException;
//import com.karate.management.karatemanagementsystem.service.exception.UserNotFoundException;
//import com.paypal.core.PayPalHttpClient;
//import com.paypal.http.HttpResponse;
//import com.paypal.orders.Order;
//import com.paypal.orders.OrdersCreateRequest;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.io.IOException;
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class PayPalServiceTest {
//    @Mock
//    private PayPalHttpClient payPalHttpClient;
//
//    @Mock
//    private PaymentRepository paymentRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @InjectMocks
//    private PayPalService payPalService;
//
//    private UserEntity userEntity;
//    private PaymentEntity paymentEntity;
//
//    @BeforeEach
//    void setUp() {
//        userEntity = new UserEntity();
//        userEntity.setUserId(1L);
//        userEntity.setUsername("testuser");
//
//        paymentEntity = new PaymentEntity();
//        paymentEntity.setPaymentId(100L);
//        paymentEntity.setPaymentStatus(PaymentStatus.PENDING);
//        paymentEntity.setAmount(new BigDecimal("150.00"));
//        paymentEntity.setPaymentDate(LocalDate.now());
//        paymentEntity.setUserEntity(userEntity);
//        userEntity.getPaymentEntities().add(paymentEntity);
//    }
//
//    @Test
//    void should_throw_user_not_found_exception_when_user_not_found_while_creating_payment() {
//        // given
//        Long userId = 1L;
//        when(userRepository.findById(userId)).thenReturn(Optional.empty());
//
//        // when
//        UserNotFoundException exception = assertThrows(
//                UserNotFoundException.class, () -> payPalService.createPayment(userId)
//        );
//
//        // then
//        assertEquals("User not found", exception.getMessage());
//    }
//
//    @SuppressWarnings("unchecked")
//    @Test
//    void should_create_payment_and_return_dto_when_user_exists() throws IOException {
//        // given
//        Long userId = 1L;
//        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
//
//        Order mockOrder = mock(Order.class);
//        when(mockOrder.id()).thenReturn("mock-order-id");
//
//        HttpResponse<Order> mockResponse = (HttpResponse<Order>) mock(HttpResponse.class);
//        when(mockResponse.result()).thenReturn(mockOrder);
//        when(payPalHttpClient.execute(any(OrdersCreateRequest.class))).thenReturn(mockResponse);
//
//        // when
//        PaymentResponseDto responseDto = payPalService.createPayment(userId);
//
//        // then
//        verify(paymentRepository, times(1)).save(any(PaymentEntity.class));
//        assertNotNull(responseDto);
//        assertEquals("mock-order-id", responseDto.paymentId());
//        assertEquals(userId, responseDto.userId());
//        assertEquals(PaymentStatus.PENDING, responseDto.status());
//    }
//
//
//    @Test
//    void should_throw_payment_not_found_exception_when_payment_does_not_exist() {
//        // given
//        String paymentId = "12345";
//        when(paymentRepository.findById(Long.parseLong(paymentId))).thenReturn(Optional.empty());
//
//        // when
//        PaymentNotFoundException exception = assertThrows(
//                PaymentNotFoundException.class, () -> payPalService.confirmPayment(paymentId)
//        );
//
//        // then
//        assertEquals("Payment not found", exception.getMessage());
//    }
//
//    @Test
//    void should_throw_payment_already_confirmed_exception_when_payment_is_already_paid() {
//        // given
//        paymentEntity.setPaymentStatus(PaymentStatus.PAID);
//        when(paymentRepository.findById(paymentEntity.getPaymentId())).thenReturn(Optional.of(paymentEntity));
//
//        // when
//        PaymentAlreadyConfirmed exception = assertThrows(
//                PaymentAlreadyConfirmed.class, () -> payPalService.confirmPayment(String.valueOf(paymentEntity.getPaymentId()))
//        );
//
//        // then
//        assertEquals("Payment already confirmed", exception.getMessage());
//    }
//
//    @Test
//    void should_update_payment_status_and_return_dto_when_payment_is_confirmed() {
//        // given
//        when(paymentRepository.findById(paymentEntity.getPaymentId())).thenReturn(Optional.of(paymentEntity));
//
//        // when
//        PaymentResponseDto responseDto = payPalService.confirmPayment(String.valueOf(paymentEntity.getPaymentId()));
//
//        // then
//        verify(paymentRepository, times(1)).save(paymentEntity);
//        assertEquals(PaymentStatus.PAID, responseDto.status());
//        assertEquals(paymentEntity.getPaymentId().toString(), responseDto.paymentId());
//        assertEquals(paymentEntity.getUserEntity().getUserId(), responseDto.userId());
//    }
//}
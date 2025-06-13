package com.karate.management.karatemanagementsystem.infrastructure.scheduler;

import com.karate.management.karatemanagementsystem.domain.payment.PaymentEntity;
import com.karate.management.karatemanagementsystem.user.domain.model.UserEntity;
import com.karate.management.karatemanagementsystem.domain.payment.PaymentRepository;
import com.karate.management.karatemanagementsystem.user.domain.repository.UserRepository;
import com.karate.management.karatemanagementsystem.domain.payment.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class PaymentServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    private UserEntity user1;
    private UserEntity user2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user1 = new UserEntity();
        user1.setUserId(1L);
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");

        user2 = new UserEntity();
        user2.setUserId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
    }

    @Test
    void should_return_users_with_outstanding_payments() {
        // given
        user1.setRegistrationDate(LocalDate.now().minusMonths(3));
        user2.setRegistrationDate(LocalDate.now().minusMonths(1));

        PaymentEntity payment1 = new PaymentEntity();
        payment1.setUserEntity(user1);
        payment1.setPaymentDate(LocalDate.of(YearMonth.now().getYear(), YearMonth.now().getMonthValue(), 1));
        payment1.setPaymentStatus(PaymentStatus.PAID);

        PaymentEntity payment2 = new PaymentEntity();
        payment2.setUserEntity(user1);
        payment2.setPaymentDate(LocalDate.of(YearMonth.now().minusMonths(1).getYear(), YearMonth.now().minusMonths(1).getMonthValue(), 1));
        payment2.setPaymentStatus(PaymentStatus.PAID);

        PaymentEntity payment3 = new PaymentEntity();
        payment3.setUserEntity(user1);
        payment3.setPaymentDate(LocalDate.of(YearMonth.now().minusMonths(2).getYear(), YearMonth.now().minusMonths(2).getMonthValue(), 1));
        payment3.setPaymentStatus(PaymentStatus.PAID);

        PaymentEntity payment4 = new PaymentEntity();
        payment4.setUserEntity(user1);
        payment4.setPaymentDate(LocalDate.of(YearMonth.now().minusMonths(3).getYear(), YearMonth.now().minusMonths(3).getMonthValue(), 1));
        payment4.setPaymentStatus(PaymentStatus.PAID);

        PaymentEntity payment5 = new PaymentEntity();
        payment5.setUserEntity(user2);
        payment5.setPaymentDate(LocalDate.of(YearMonth.now().getYear(), YearMonth.now().getMonthValue(), 1));
        payment5.setPaymentStatus(PaymentStatus.PAID);

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));
        when(paymentRepository.findPaymentsByUserEntityUserId(user1.getUserId())).thenReturn(List.of(payment1, payment2, payment3, payment4));
        when(paymentRepository.findPaymentsByUserEntityUserId(user2.getUserId())).thenReturn(List.of(payment5));

        // when
        List<UserEntity> result = paymentService.getUsersWithOutstandingPayments();

        // then
        assertEquals(1, result.size());
        assertEquals(user2.getEmail(), result.get(0).getEmail());
    }

    @Test
    void should_return_empty_list_when_user_has_no_outstanding_payments() {
        // given
        user1.setRegistrationDate(LocalDate.now().minusMonths(1));
        user2.setRegistrationDate(LocalDate.now());

        PaymentEntity payment1 = new PaymentEntity();
        payment1.setUserEntity(user1);
        payment1.setPaymentDate(LocalDate.of(YearMonth.now().minusMonths(1).getYear(), YearMonth.now().minusMonths(1).getMonthValue(), 1));
        payment1.setPaymentStatus(PaymentStatus.PAID);

        PaymentEntity payment2 = new PaymentEntity();
        payment2.setUserEntity(user1);
        payment2.setPaymentDate(LocalDate.of(YearMonth.now().getYear(), YearMonth.now().getMonthValue(), 1));
        payment2.setPaymentStatus(PaymentStatus.PAID);

        PaymentEntity payment3 = new PaymentEntity();
        payment3.setUserEntity(user2);
        payment3.setPaymentDate(LocalDate.of(YearMonth.now().getYear(), YearMonth.now().getMonthValue(), 1));
        payment3.setPaymentStatus(PaymentStatus.PAID);

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));
        when(paymentRepository.findPaymentsByUserEntityUserId(user1.getUserId())).thenReturn(List.of(payment1, payment2));
        when(paymentRepository.findPaymentsByUserEntityUserId(user2.getUserId())).thenReturn(List.of(payment3));

        // when
        List<UserEntity> result = paymentService.getUsersWithOutstandingPayments();

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void should_return_unpaid_months_for_user() {
        // given
        user1.setRegistrationDate(LocalDate.now().minusMonths(3));

        PaymentEntity payment1 = new PaymentEntity();
        payment1.setUserEntity(user1);
        payment1.setPaymentDate(LocalDate.of(YearMonth.now().minusMonths(1).getYear(), YearMonth.now().minusMonths(1).getMonthValue(), 1));
        payment1.setPaymentStatus(PaymentStatus.PAID);

        when(paymentRepository.findPaymentsByUserEntityUserId(user1.getUserId())).thenReturn(List.of(payment1));

        // when
        List<YearMonth> unpaidMonths = paymentService.getUnpaidMonthsForUser(user1);

        // then
        assertEquals(3, unpaidMonths.size());
        assertTrue(unpaidMonths.contains(YearMonth.now()));
    }

    @Test
    void should_return_zero_unpaid_months_for_user_with_no_outstanding_payments() {
        // given
        user1.setRegistrationDate(LocalDate.now());

        PaymentEntity payment1 = new PaymentEntity();
        payment1.setUserEntity(user1);
        payment1.setPaymentDate(LocalDate.of(YearMonth.now().getYear(), YearMonth.now().getMonthValue(), 1));
        payment1.setPaymentStatus(PaymentStatus.PAID);

        when(paymentRepository.findPaymentsByUserEntityUserId(user1.getUserId())).thenReturn(List.of(payment1));

        // when
        List<YearMonth> unpaidMonths = paymentService.getUnpaidMonthsForUser(user1);

        // then
        assertTrue(unpaidMonths.isEmpty());
    }

    @Test
    void should_calculate_total_debt_for_user() {
        // given
        user1.setRegistrationDate(LocalDate.now().minusMonths(3));

        PaymentEntity payment1 = new PaymentEntity();
        payment1.setUserEntity(user1);
        payment1.setPaymentDate(LocalDate.of(YearMonth.now().minusMonths(1).getYear(), YearMonth.now().minusMonths(1).getMonthValue(), 1));

        when(paymentRepository.findPaymentsByUserEntityUserId(user1.getUserId())).thenReturn(List.of(payment1));

        // when
        double totalDebt = paymentService.calculateTotalDebt(user1);

        // then
        assertEquals(4 * 150.00, totalDebt);
    }

    @Test
    void should_calculate_zero_debt_for_user_with_no_outstanding_payments() {
        // given
        user1.setRegistrationDate(LocalDate.now());

        PaymentEntity payment1 = new PaymentEntity();
        payment1.setUserEntity(user1);
        payment1.setPaymentDate(LocalDate.of(YearMonth.now().getYear(), YearMonth.now().getMonthValue(), 1));
        payment1.setPaymentStatus(PaymentStatus.PAID);

        when(paymentRepository.findPaymentsByUserEntityUserId(user1.getUserId())).thenReturn(List.of(payment1));

        // when
        double totalDebt = paymentService.calculateTotalDebt(user1);

        // then
        assertEquals(0.0, totalDebt);
    }
}
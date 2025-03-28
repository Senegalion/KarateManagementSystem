package com.karate.management.karatemanagementsystem.infrastructure.scheduler;

import com.karate.management.karatemanagementsystem.model.entity.UserEntity;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.time.YearMonth;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(OutputCaptureExtension.class)
class PaymentReminderSchedulerTest {
    @Mock
    private PaymentService paymentService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PaymentReminderScheduler scheduler;

    private UserEntity user1;
    private UserEntity user2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user1 = new UserEntity();
        user1.setUserId(1L);
        user1.setUsername("User1");
        user1.setEmail("user1@example.com");

        user2 = new UserEntity();
        user2.setUserId(2L);
        user2.setUsername("User2");
        user2.setEmail("user2@example.com");
    }

    @Test
    void should_send_payment_reminders_to_users_with_debts() throws MessagingException {
        // given
        when(paymentService.getUsersWithOutstandingPayments()).thenReturn(List.of(user1, user2));
        when(paymentService.getUnpaidMonthsForUser(any())).thenReturn(List.of(YearMonth.now().minusMonths(1)));
        when(paymentService.calculateTotalDebt(any())).thenReturn(150.00);

        // when
        scheduler.sendPaymentReminders();

        // then
        verify(emailService, times(2)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void should_not_send_emails_when_no_users_have_debts() throws MessagingException {
        // given
        when(paymentService.getUsersWithOutstandingPayments()).thenReturn(Collections.emptyList());

        // when
        scheduler.sendPaymentReminders();

        // then
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void should_not_send_email_if_user_has_no_unpaid_months() throws MessagingException {
        // given
        when(paymentService.getUsersWithOutstandingPayments()).thenReturn(List.of(user1));
        when(paymentService.getUnpaidMonthsForUser(user1)).thenReturn(Collections.emptyList());

        // when
        scheduler.sendPaymentReminders();

        // then
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void should_handle_users_with_different_debt_amounts() throws MessagingException {
        // given
        when(paymentService.getUsersWithOutstandingPayments()).thenReturn(List.of(user1, user2));
        when(paymentService.getUnpaidMonthsForUser(user1)).thenReturn(List.of(YearMonth.now().minusMonths(1)));
        when(paymentService.getUnpaidMonthsForUser(user2)).thenReturn(List.of(YearMonth.now().minusMonths(2), YearMonth.now().minusMonths(3)));
        when(paymentService.calculateTotalDebt(user1)).thenReturn(150.00);
        when(paymentService.calculateTotalDebt(user2)).thenReturn(300.00);

        // when
        scheduler.sendPaymentReminders();

        // then
        verify(emailService, times(2)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void should_log_when_no_users_have_debts(CapturedOutput capturedOutput) throws MessagingException {
        // given
        when(paymentService.getUsersWithOutstandingPayments()).thenReturn(Collections.emptyList());

        // when
        scheduler.sendPaymentReminders();

        // then
        assertTrue(capturedOutput.getOut().contains("No users with outstanding payments."));
    }
}
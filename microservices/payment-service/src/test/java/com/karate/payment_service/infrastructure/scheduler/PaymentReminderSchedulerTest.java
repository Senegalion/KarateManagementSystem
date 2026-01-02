package com.karate.payment_service.infrastructure.scheduler;

import com.karate.payment_service.domain.model.UserAccountEntity;
import com.karate.payment_service.domain.repository.UserAccountRepository;
import com.karate.payment_service.domain.service.PaymentsConfig;
import com.karate.payment_service.domain.service.UnpaidCalculator;
import com.karate.payment_service.infrastructure.messaging.PaymentEventPublisher;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.mockito.Mockito.*;

class PaymentReminderSchedulerTest {

    @Test
    void monthlyReminder_publishesOnlyForUsersWithDebt() {
        PaymentsConfig cfg = new PaymentsConfig();
        cfg.setMonthlyFee(new BigDecimal("60.00"));
        cfg.setCurrency("PLN");

        UserAccountRepository users = mock(UserAccountRepository.class);
        UnpaidCalculator calc = mock(UnpaidCalculator.class);
        PaymentEventPublisher pub = mock(PaymentEventPublisher.class);

        UserAccountEntity u1 = UserAccountEntity.builder().userId(1L).email("a@b.com").username("a")
                .registrationDate(LocalDate.now().minusMonths(1)).build();
        UserAccountEntity u2 = UserAccountEntity.builder().userId(2L).email("c@d.com").username("c")
                .registrationDate(LocalDate.now().minusMonths(1)).build();

        when(users.findAll()).thenReturn(List.of(u1, u2));
        when(calc.unpaidMonths(u1)).thenReturn(List.of());
        when(calc.unpaidMonths(u2)).thenReturn(List.of(YearMonth.now().minusMonths(1), YearMonth.now()));

        PaymentReminderScheduler s = new PaymentReminderScheduler(cfg, users, calc, pub);

        s.monthlyReminder();

        verify(pub, times(1)).publishReminder(any());
    }
}

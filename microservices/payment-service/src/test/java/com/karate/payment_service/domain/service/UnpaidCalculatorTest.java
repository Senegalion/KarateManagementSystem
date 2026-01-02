package com.karate.payment_service.domain.service;

import com.karate.payment_service.domain.model.PaymentItemEntity;
import com.karate.payment_service.domain.model.PaymentStatus;
import com.karate.payment_service.domain.model.UserAccountEntity;
import com.karate.payment_service.domain.repository.PaymentItemRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UnpaidCalculatorTest {

    @Test
    void unpaidMonths_returnsAllFromRegistrationToNow_whenNonePaid() {
        PaymentItemRepository repo = mock(PaymentItemRepository.class);
        when(repo.findByUserIdAndStatus(1L, PaymentStatus.PAID)).thenReturn(List.of());

        UnpaidCalculator calc = new UnpaidCalculator(repo);

        UserAccountEntity u = UserAccountEntity.builder()
                .userId(1L)
                .email("a@b.com")
                .username("u")
                .registrationDate(LocalDate.now().minusMonths(2))
                .build();

        var out = calc.unpaidMonths(u);
        assertThat(out).isNotEmpty();
        assertThat(out.get(0)).isEqualTo(YearMonth.from(u.getRegistrationDate()));
        assertThat(out.get(out.size() - 1)).isEqualTo(YearMonth.now());
    }

    @Test
    void unpaidMonths_skipsPaidMonths() {
        PaymentItemRepository repo = mock(PaymentItemRepository.class);

        YearMonth reg = YearMonth.now().minusMonths(2);
        YearMonth paid = reg.plusMonths(1);

        PaymentItemEntity item = PaymentItemEntity.builder()
                .userId(1L)
                .yearMonth(paid)
                .status(PaymentStatus.PAID)
                .build();

        when(repo.findByUserIdAndStatus(1L, PaymentStatus.PAID)).thenReturn(List.of(item));

        UnpaidCalculator calc = new UnpaidCalculator(repo);

        UserAccountEntity u = UserAccountEntity.builder()
                .userId(1L)
                .registrationDate(reg.atDay(1))
                .email("x")
                .username("y")
                .build();

        var out = calc.unpaidMonths(u);

        assertThat(out).doesNotContain(paid);
        assertThat(out).contains(reg, YearMonth.now());
    }
}

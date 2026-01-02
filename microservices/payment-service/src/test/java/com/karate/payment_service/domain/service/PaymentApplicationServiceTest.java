package com.karate.payment_service.domain.service;

import com.karate.payment_service.api.dto.CreateOrderRequest;
import com.karate.payment_service.domain.model.*;
import com.karate.payment_service.domain.repository.PaymentItemRepository;
import com.karate.payment_service.domain.repository.PaymentRepository;
import com.karate.payment_service.domain.repository.UserAccountRepository;
import com.karate.payment_service.infrastructure.messaging.PaymentEventPublisher;
import com.karate.payment_service.infrastructure.paypal.PayPalClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PaymentApplicationServiceTest {

    PaymentsConfig cfg = new PaymentsConfig();
    UserAccountRepository users = mock(UserAccountRepository.class);
    PaymentRepository payments = mock(PaymentRepository.class);
    PaymentItemRepository items = mock(PaymentItemRepository.class);
    UnpaidCalculator calc = mock(UnpaidCalculator.class);
    PayPalClient payPal = mock(PayPalClient.class);
    PaymentEventPublisher publisher = mock(PaymentEventPublisher.class);

    PaymentApplicationService service;

    @BeforeEach
    void setup() {
        cfg.setMonthlyFee(new BigDecimal("60.00"));
        cfg.setCurrency("PLN");
        service = new PaymentApplicationService(cfg, users, payments, items, calc, payPal, publisher);
    }

    private UserAccountEntity user(long id, LocalDate reg) {
        return UserAccountEntity.builder()
                .userId(id).email("a@b.com").username("john").registrationDate(reg)
                .clubId(1L).clubName("Club").karateRank("WHITE")
                .build();
    }

    @Test
    void getUnpaidSummary_throws_whenUserMissing() {
        when(users.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getUnpaidSummary(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getUnpaidSummary_returnsMonthsAndTotals() {
        var u = user(1L, LocalDate.now().minusMonths(2));
        when(users.findById(1L)).thenReturn(Optional.of(u));
        when(calc.unpaidMonths(u)).thenReturn(List.of(YearMonth.of(2025, 1), YearMonth.of(2025, 2)));

        var out = service.getUnpaidSummary(1L);

        assertThat(out.months()).containsExactly("2025-01", "2025-02");
        assertThat(out.monthlyFee()).isEqualByComparingTo("60.00");
        assertThat(out.total()).isEqualByComparingTo("120.00");
    }

    @Test
    void history_mapsEntitiesToDtos() {
        PaymentEntity p = PaymentEntity.builder()
                .paymentId(10L)
                .userId(1L)
                .provider(PaymentProvider.PAYPAL)
                .providerOrderId("OID")
                .currency("PLN")
                .amount(new BigDecimal("120.00"))
                .status(PaymentStatus.PAID)
                .createdAt(Instant.parse("2025-01-01T00:00:00Z"))
                .paidAt(Instant.parse("2025-01-01T00:01:00Z"))
                .build();

        PaymentItemEntity i1 = PaymentItemEntity.builder().payment(p).userId(1L).yearMonth(YearMonth.of(2025, 1)).amount(cfg.getMonthlyFee()).status(PaymentStatus.PAID).build();
        PaymentItemEntity i2 = PaymentItemEntity.builder().payment(p).userId(1L).yearMonth(YearMonth.of(2025, 2)).amount(cfg.getMonthlyFee()).status(PaymentStatus.PAID).build();
        p.setItems(List.of(i1, i2));

        when(payments.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(p));

        var out = service.history(1L);

        assertThat(out).hasSize(1);
        assertThat(out.get(0).provider()).isEqualTo("PAYPAL");
        assertThat(out.get(0).months()).containsExactly("2025-01", "2025-02");
    }

    @Test
    void createOrder_defaultsMonthsAndCurrency_andSavesPayment() throws Exception {
        var u = user(1L, LocalDate.now().minusMonths(1));
        when(users.findById(1L)).thenReturn(Optional.of(u));
        when(payPal.createOrder(anyMap())).thenReturn("ORDER123");
        when(items.existsByUserIdAndYearMonthAndStatus(anyLong(), any(), eq(PaymentStatus.PAID))).thenReturn(false);

        CreateOrderRequest req = new CreateOrderRequest(1L, null, null, null, null);

        var out = service.createOrder(req);

        assertThat(out.providerOrderId()).isEqualTo("ORDER123");
        assertThat(out.approvalUrl()).contains("token=ORDER123");
        assertThat(out.currency()).isEqualTo("PLN");
        assertThat(out.amount()).isEqualByComparingTo("60.00");
        assertThat(out.months()).hasSize(1);

        ArgumentCaptor<PaymentEntity> payCap = ArgumentCaptor.forClass(PaymentEntity.class);
        verify(payments).save(payCap.capture());
        assertThat(payCap.getValue().getItems()).hasSize(1);
        assertThat(payCap.getValue().getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void createOrder_throws_whenMonthAlreadyPaid() {
        var u = user(1L, LocalDate.now().minusMonths(1));
        when(users.findById(1L)).thenReturn(Optional.of(u));
        when(items.existsByUserIdAndYearMonthAndStatus(eq(1L), any(), eq(PaymentStatus.PAID))).thenReturn(true);

        CreateOrderRequest req = new CreateOrderRequest(1L, List.of(YearMonth.of(2025, 1)), "PLN", null, null);

        assertThatThrownBy(() -> service.createOrder(req))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Month already paid");
    }

    @Test
    void createOrder_wrapsIOException() throws Exception {
        var u = user(1L, LocalDate.now().minusMonths(1));
        when(users.findById(1L)).thenReturn(Optional.of(u));
        when(items.existsByUserIdAndYearMonthAndStatus(anyLong(), any(), eq(PaymentStatus.PAID))).thenReturn(false);
        when(payPal.createOrder(anyMap())).thenThrow(new IOException("boom"));

        CreateOrderRequest req = new CreateOrderRequest(1L, List.of(YearMonth.of(2025, 1)), null, null, null);

        assertThatThrownBy(() -> service.createOrder(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("PayPal create error");
    }

    @Test
    void capture_returnsAlreadyPaid_whenPaymentPaid() {
        PaymentEntity p = PaymentEntity.builder()
                .userId(1L).provider(PaymentProvider.PAYPAL).providerOrderId("OID")
                .currency("PLN").amount(new BigDecimal("60.00"))
                .status(PaymentStatus.PAID).createdAt(Instant.now())
                .items(List.of())
                .build();
        when(payments.lockByProviderOrderId("OID")).thenReturn(Optional.of(p));

        var out = service.capture("OID");

        assertThat(out.status()).isEqualTo("ALREADY_PAID");
        verifyNoInteractions(payPal);
        verifyNoInteractions(publisher);
    }

    @Test
    void capture_happyPath_setsPaidAndPublishes() throws Exception {
        PaymentEntity p = PaymentEntity.builder()
                .userId(1L).provider(PaymentProvider.PAYPAL).providerOrderId("OID")
                .currency("PLN").amount(new BigDecimal("120.00"))
                .status(PaymentStatus.PENDING).createdAt(Instant.now())
                .build();

        PaymentItemEntity i1 = PaymentItemEntity.builder().payment(p).userId(1L).yearMonth(YearMonth.of(2025, 1)).amount(cfg.getMonthlyFee()).status(PaymentStatus.PENDING).build();
        PaymentItemEntity i2 = PaymentItemEntity.builder().payment(p).userId(1L).yearMonth(YearMonth.of(2025, 2)).amount(cfg.getMonthlyFee()).status(PaymentStatus.PENDING).build();
        p.setItems(List.of(i1, i2));

        when(payments.lockByProviderOrderId("OID")).thenReturn(Optional.of(p));
        when(payPal.captureOrder("OID")).thenReturn(true);

        var out = service.capture("OID");

        assertThat(out.status()).isEqualTo("PAID");
        assertThat(p.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(p.getPaidAt()).isNotNull();
        assertThat(p.getItems()).allMatch(it -> it.getStatus() == PaymentStatus.PAID);

        verify(payments).save(p);
        verify(publisher).publishReceived(any());
    }

    @Test
    void capture_throws_whenPayPalRejected() throws Exception {
        PaymentEntity p = PaymentEntity.builder()
                .userId(1L).provider(PaymentProvider.PAYPAL).providerOrderId("OID")
                .currency("PLN").amount(new BigDecimal("60.00"))
                .status(PaymentStatus.PENDING).createdAt(Instant.now())
                .items(List.of(PaymentItemEntity.builder().yearMonth(YearMonth.of(2025, 1)).status(PaymentStatus.PENDING).build()))
                .build();
        when(payments.lockByProviderOrderId("OID")).thenReturn(Optional.of(p));
        when(payPal.captureOrder("OID")).thenReturn(false);

        assertThatThrownBy(() -> service.capture("OID"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("PayPal capture rejected");
    }

    @Test
    void manualPayment_happyPath_savesAndPublishes() {
        var u = user(1L, LocalDate.now().minusMonths(1));
        when(users.findById(1L)).thenReturn(Optional.of(u));
        when(items.existsByUserIdAndYearMonthAndStatus(eq(1L), any(), eq(PaymentStatus.PAID))).thenReturn(false);

        service.manualPayment(1L, List.of(YearMonth.of(2025, 1), YearMonth.of(2025, 2)));

        verify(payments).save(any());
        verify(publisher).publishReceived(any());
    }

    @Test
    void manualPayment_throws_whenAlreadyPaidMonth() {
        var u = user(1L, LocalDate.now().minusMonths(1));
        when(users.findById(1L)).thenReturn(Optional.of(u));
        when(items.existsByUserIdAndYearMonthAndStatus(eq(1L), any(), eq(PaymentStatus.PAID))).thenReturn(true);

        assertThatThrownBy(() -> service.manualPayment(1L, List.of(YearMonth.of(2025, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Month already paid");
    }
}

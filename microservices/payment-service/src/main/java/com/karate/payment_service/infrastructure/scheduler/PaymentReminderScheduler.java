package com.karate.payment_service.infrastructure.scheduler;

import com.karate.payment_service.domain.repository.UserAccountRepository;
import com.karate.payment_service.domain.service.PaymentsConfig;
import com.karate.payment_service.domain.service.UnpaidCalculator;
import com.karate.payment_service.infrastructure.messaging.PaymentEventPublisher;
import com.karate.payment_service.infrastructure.messaging.dto.PaymentDebtReminderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class PaymentReminderScheduler {

    private final PaymentsConfig cfg;
    private final UserAccountRepository users;
    private final UnpaidCalculator calc;
    private final PaymentEventPublisher publisher;

    @Scheduled(cron = "${payments.reminder.cron}")
    public void monthlyReminder() {
        log.info("Monthly debt reminder started");
        var all = users.findAll();
        int reminded = 0;
        for (var u : all) {
            var unpaid = calc.unpaidMonths(u);
            if (unpaid.isEmpty()) continue;

            var months = unpaid.stream().map(Object::toString).toList();
            var total = cfg.getMonthlyFee().multiply(BigDecimal.valueOf(months.size()));

            publisher.publishReminder(PaymentDebtReminderEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("PAYMENT_DEBT_REMINDER")
                    .timestamp(Instant.now())
                    .userId(u.getUserId())
                    .email(u.getEmail())
                    .monthlyFee(cfg.getMonthlyFee())
                    .total(total)
                    .months(months)
                    .build());
            reminded++;
        }
        log.info("Monthly debt reminder finished, users reminded={}", reminded);
    }
}


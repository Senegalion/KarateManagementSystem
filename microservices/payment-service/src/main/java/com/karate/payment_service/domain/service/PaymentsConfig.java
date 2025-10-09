package com.karate.payment_service.domain.service;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Data
@Component
@ConfigurationProperties(prefix = "payments")
public class PaymentsConfig {
    private BigDecimal monthlyFee;
    private String currency;
    private Reminder reminder = new Reminder();

    @Data
    public static class Reminder {
        private String cron;
    }
}

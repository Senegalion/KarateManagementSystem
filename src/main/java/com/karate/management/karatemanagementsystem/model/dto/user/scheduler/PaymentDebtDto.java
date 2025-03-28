package com.karate.management.karatemanagementsystem.model.dto.user.scheduler;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record PaymentDebtDto(
        Long userId,
        BigDecimal amount,
        String status
) {
}

package com.karate.payment_service.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record UnpaidSummaryDto(
        List<String> months,
        BigDecimal monthlyFee,
        BigDecimal total
) {
}

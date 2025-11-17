package com.karate.payment_service.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Summary of unpaid months and expected costs.")
public record UnpaidSummaryDto(

        @Schema(description = "List of unpaid months.", example = "[\"2025-01\"]")
        List<String> months,

        @Schema(description = "Fee per month.", example = "60.00")
        BigDecimal monthlyFee,

        @Schema(description = "Total amount owed.", example = "120.00")
        BigDecimal total
) {
}

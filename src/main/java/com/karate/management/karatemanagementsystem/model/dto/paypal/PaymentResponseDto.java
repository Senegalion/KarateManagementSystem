package com.karate.management.karatemanagementsystem.model.dto.paypal;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.karate.management.karatemanagementsystem.model.staticdata.PaymentStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record PaymentResponseDto(
        String paymentId,
        Long userId,
        BigDecimal amount,
        PaymentStatus status,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate paymentDate,
        String approvalUrl
) {
}

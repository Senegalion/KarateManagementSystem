package com.karate.payment_service.domain.model;

import com.karate.payment_service.infrastructure.jpa.YearMonthAttributeConverter;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.YearMonth;

@Entity
@Table(name = "payment_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private PaymentEntity payment;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Convert(converter = YearMonthAttributeConverter.class)
    @Column(name = "year_month", nullable = false, length = 7)
    private YearMonth yearMonth;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PaymentStatus status;
}

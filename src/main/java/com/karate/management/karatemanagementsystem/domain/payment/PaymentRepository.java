package com.karate.management.karatemanagementsystem.domain.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    Optional<PaymentEntity> findByPaypalOrderId(String paypalOrderId);

    List<PaymentEntity> findPaymentsByUserEntityUserId(Long userId);
}

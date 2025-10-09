package com.karate.payment_service.domain.repository;

import com.karate.payment_service.domain.model.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    Optional<PaymentEntity> findByProviderOrderId(String providerOrderId);

    List<PaymentEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
}

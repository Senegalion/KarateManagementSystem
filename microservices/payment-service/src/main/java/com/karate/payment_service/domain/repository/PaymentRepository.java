package com.karate.payment_service.domain.repository;

import com.karate.payment_service.domain.model.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    Optional<PaymentEntity> findByProviderOrderId(String providerOrderId);

    List<PaymentEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    void deleteByUserId(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from PaymentEntity p where p.userId = :userId")
    int deleteAllByUserId(@Param("userId") Long userId);
}

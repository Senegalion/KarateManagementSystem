package com.karate.payment_service.domain.repository;

import com.karate.payment_service.domain.model.PaymentItemEntity;
import com.karate.payment_service.domain.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;

@Repository
public interface PaymentItemRepository extends JpaRepository<PaymentItemEntity, Long> {
    List<PaymentItemEntity> findByUserIdAndStatus(Long userId, PaymentStatus status);

    boolean existsByUserIdAndYearMonthAndStatus(Long userId, YearMonth ym, PaymentStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from PaymentItemEntity ua where ua.userId = :userId")
    int deleteByUserId(@Param("userId") Long userId);
}

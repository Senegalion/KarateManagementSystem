package com.karate.payment_service.domain.repository;

import com.karate.payment_service.domain.model.UserAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccountEntity, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from UserAccountEntity ua where ua.userId = :userId")
    int deleteByUserId(@Param("userId") Long userId);
}

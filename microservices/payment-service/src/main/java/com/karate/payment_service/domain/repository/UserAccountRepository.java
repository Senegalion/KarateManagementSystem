package com.karate.payment_service.domain.repository;

import com.karate.payment_service.domain.model.UserAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccountEntity, Long> {
}

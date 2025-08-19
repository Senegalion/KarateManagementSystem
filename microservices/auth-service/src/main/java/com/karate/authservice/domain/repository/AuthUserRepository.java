package com.karate.authservice.domain.repository;

import com.karate.authservice.domain.model.AuthUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthUserRepository extends JpaRepository<AuthUserEntity, Long> {
    Optional<AuthUserEntity> findByUsername(String username);

    Optional<AuthUserEntity> getUserByUsername(String username);

    Optional<AuthUserEntity> findByUserId(Long userId);
}

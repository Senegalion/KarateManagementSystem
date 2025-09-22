package com.karate.userservice.domain.repository;

import com.karate.userservice.domain.model.UserEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    List<UserEntity> findAllByKarateClubId(Long karateClubId);

    @EntityGraph(attributePaths = "addressEntity")
    Optional<UserEntity> findWithAddressByUserId(Long userId);
}

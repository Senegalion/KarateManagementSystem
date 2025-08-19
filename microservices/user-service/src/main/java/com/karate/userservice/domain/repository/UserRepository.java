package com.karate.userservice.domain.repository;

import com.karate.userservice.domain.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    List<UserEntity> findAllByKarateClubId(Long karateClubId);
}

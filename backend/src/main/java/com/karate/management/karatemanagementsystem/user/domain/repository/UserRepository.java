package com.karate.management.karatemanagementsystem.user.domain.repository;

import com.karate.management.karatemanagementsystem.user.domain.model.KarateClubEntity;
import com.karate.management.karatemanagementsystem.user.domain.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> getUserByUsername(String username);

    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.karateClub LEFT JOIN FETCH u.addressEntity WHERE u.username = :username")
    Optional<UserEntity> findByUsernameWithDetails(@Param("username") String username);

    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.trainingSessionEntities WHERE u.userId = :userId")
    Optional<UserEntity> findByIdWithTrainingSessions(@Param("userId") Long userId);

    List<UserEntity> findAllByKarateClub(KarateClubEntity karateClub);
}

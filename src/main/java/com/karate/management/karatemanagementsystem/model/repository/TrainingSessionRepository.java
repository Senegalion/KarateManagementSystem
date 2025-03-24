package com.karate.management.karatemanagementsystem.model.repository;

import com.karate.management.karatemanagementsystem.model.entity.TrainingSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrainingSessionRepository extends JpaRepository<TrainingSessionEntity, Long> {
    @Query("SELECT ts FROM TrainingSessionEntity ts JOIN FETCH ts.userEntities WHERE ts.trainingSessionId = :sessionId")
    Optional<TrainingSessionEntity> findByIdWithUsers(@Param("sessionId") Long sessionId);
}

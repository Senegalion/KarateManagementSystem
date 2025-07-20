package com.karate.training_service.domain.repository;

import com.karate.training_service.domain.model.TrainingSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingSessionRepository extends JpaRepository<TrainingSessionEntity, Long> {
    List<TrainingSessionEntity> findAllByClubId(Long clubId);
}

package com.karate.management.karatemanagementsystem.feedback.domain.repository;

import com.karate.management.karatemanagementsystem.feedback.domain.model.FeedbackEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<FeedbackEntity, Long> {
    Optional<FeedbackEntity> findByTrainingSessionEntityTrainingSessionId(Long sessionId);
}

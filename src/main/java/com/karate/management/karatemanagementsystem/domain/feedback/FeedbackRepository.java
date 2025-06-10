package com.karate.management.karatemanagementsystem.domain.feedback;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<FeedbackEntity, Long> {
    Optional<FeedbackEntity> findByTrainingSessionEntityTrainingSessionId(Long sessionId);
}

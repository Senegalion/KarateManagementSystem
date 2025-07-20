package com.karate.feedback_service.domain.repository;

import com.karate.feedback_service.domain.model.FeedbackEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<FeedbackEntity, Long> {
    Optional<FeedbackEntity> findByUserIdAndTrainingSessionId(Long userId, Long trainingSessionId);
}

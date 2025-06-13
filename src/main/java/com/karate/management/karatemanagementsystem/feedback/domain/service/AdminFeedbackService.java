package com.karate.management.karatemanagementsystem.feedback.domain.service;

import com.karate.management.karatemanagementsystem.feedback.domain.model.FeedbackEntity;
import com.karate.management.karatemanagementsystem.feedback.domain.repository.FeedbackRepository;
import com.karate.management.karatemanagementsystem.feedback.api.dto.FeedbackRequestDto;
import com.karate.management.karatemanagementsystem.feedback.api.dto.FeedbackResponseDto;
import com.karate.management.karatemanagementsystem.training.domain.model.TrainingSessionEntity;
import com.karate.management.karatemanagementsystem.domain.user.UserEntity;
import com.karate.management.karatemanagementsystem.training.domain.repository.TrainingSessionRepository;
import com.karate.management.karatemanagementsystem.domain.user.UserRepository;
import com.karate.management.karatemanagementsystem.training.domain.exception.TrainingSessionNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class AdminFeedbackService {
    private final UserRepository userRepository;
    private final TrainingSessionRepository trainingSessionRepository;
    private final FeedbackRepository feedbackRepository;

    @Transactional
    public FeedbackResponseDto addFeedbackToUserForTrainingSession(Long userId, Long trainingSessionId, FeedbackRequestDto feedbackRequestDto) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        TrainingSessionEntity trainingSession = trainingSessionRepository.findById(trainingSessionId)
                .orElseThrow(() -> new TrainingSessionNotFoundException("Training session not found"));

        if (!user.getTrainingSessionEntities().contains(trainingSession)) {
            throw new TrainingSessionNotFoundException("User is not enrolled in the specified training session");
        }

        FeedbackEntity feedbackEntity = new FeedbackEntity();
        feedbackEntity.setUserEntity(user);
        feedbackEntity.setTrainingSessionEntity(trainingSession);
        feedbackEntity.setComment(feedbackRequestDto.comment());
        feedbackEntity.setStarRating(feedbackRequestDto.starRating());
        FeedbackEntity savedFeedback = feedbackRepository.save(feedbackEntity);

        return new FeedbackResponseDto(savedFeedback.getComment(), savedFeedback.getStarRating());
    }
}

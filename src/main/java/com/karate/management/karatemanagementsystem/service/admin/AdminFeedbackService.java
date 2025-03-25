package com.karate.management.karatemanagementsystem.service.admin;

import com.karate.management.karatemanagementsystem.model.dto.feedback.FeedbackRequestDto;
import com.karate.management.karatemanagementsystem.model.dto.feedback.FeedbackResponseDto;
import com.karate.management.karatemanagementsystem.model.entity.FeedbackEntity;
import com.karate.management.karatemanagementsystem.model.entity.TrainingSessionEntity;
import com.karate.management.karatemanagementsystem.model.entity.UserEntity;
import com.karate.management.karatemanagementsystem.model.repository.FeedbackRepository;
import com.karate.management.karatemanagementsystem.model.repository.TrainingSessionRepository;
import com.karate.management.karatemanagementsystem.model.repository.UserRepository;
import com.karate.management.karatemanagementsystem.service.exception.TrainingSessionNotFoundException;
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

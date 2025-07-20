package com.karate.feedback_service.domain.service;

import com.karate.feedback_service.api.dto.FeedbackRequestDto;
import com.karate.feedback_service.api.dto.FeedbackResponseDto;
import com.karate.feedback_service.domain.exception.TrainingSessionNotFoundException;
import com.karate.feedback_service.domain.model.FeedbackEntity;
import com.karate.feedback_service.domain.repository.FeedbackRepository;
import com.karate.feedback_service.infrastructure.client.TrainingSessionClient;
import com.karate.feedback_service.infrastructure.client.UserClient;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class AdminFeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final UserClient userClient;
    private final TrainingSessionClient trainingSessionClient;

    @Transactional
    public FeedbackResponseDto addFeedbackToUserForTrainingSession(Long userId, Long trainingSessionId, FeedbackRequestDto feedbackRequestDto) {
        Boolean userExists = userClient.checkUserExists(userId);
        if (userExists == null || !userExists) {
            throw new UsernameNotFoundException("User not found");
        }

        Boolean trainingExists = trainingSessionClient.checkTrainingSessionExists(trainingSessionId);
        if (trainingExists == null || !trainingExists) {
            throw new TrainingSessionNotFoundException("Training session not found");
        }

        Boolean enrolled = userClient.checkUserEnrolledInSession(userId, trainingSessionId);
        if (enrolled == null || !enrolled) {
            throw new TrainingSessionNotFoundException("User is not enrolled in the specified training session");
        }

        FeedbackEntity feedbackEntity = new FeedbackEntity();
        feedbackEntity.setUserId(userId);
        feedbackEntity.setTrainingSessionId(trainingSessionId);
        feedbackEntity.setComment(feedbackRequestDto.comment());
        feedbackEntity.setStarRating(feedbackRequestDto.starRating());

        FeedbackEntity savedFeedback = feedbackRepository.save(feedbackEntity);

        return new FeedbackResponseDto(savedFeedback.getComment(), savedFeedback.getStarRating());
    }
}

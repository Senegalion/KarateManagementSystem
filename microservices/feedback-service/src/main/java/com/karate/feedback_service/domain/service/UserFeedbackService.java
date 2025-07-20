package com.karate.feedback_service.domain.service;

import com.karate.feedback_service.api.dto.FeedbackResponseDto;
import com.karate.feedback_service.domain.exception.FeedbackNotFoundException;
import com.karate.feedback_service.domain.exception.TrainingSessionNotFoundException;
import com.karate.feedback_service.domain.exception.UserNotSignedUpException;
import com.karate.feedback_service.domain.model.FeedbackEntity;
import com.karate.feedback_service.domain.repository.FeedbackRepository;
import com.karate.feedback_service.infrastructure.client.TrainingSessionClient;
import com.karate.feedback_service.infrastructure.client.UserClient;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class UserFeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final UserClient userClient;
    private final TrainingSessionClient trainingSessionClient;

    @Transactional
    public FeedbackResponseDto getFeedbackForSession(Long sessionId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("User not authenticated");
        }

        String username = authentication.getName();
        Long userId = userClient.getUserIdByUsername(username);
        if (userId == null) {
            throw new UsernameNotFoundException("User not found");
        }

        Boolean trainingExists = trainingSessionClient.checkTrainingSessionExists(sessionId);
        if (trainingExists == null || !trainingExists) {
            throw new TrainingSessionNotFoundException("Training session not found");
        }

        Boolean enrolled = userClient.checkUserEnrolledInSession(userId, sessionId);
        if (enrolled == null || !enrolled) {
            throw new UserNotSignedUpException("User is not enrolled in the specified training session");
        }

        FeedbackEntity feedback = feedbackRepository.findByUserIdAndTrainingSessionId(userId, sessionId)
                .orElseThrow(() -> new FeedbackNotFoundException("Feedback not found for this session"));

        return new FeedbackResponseDto(feedback.getComment(), feedback.getStarRating());
    }
}

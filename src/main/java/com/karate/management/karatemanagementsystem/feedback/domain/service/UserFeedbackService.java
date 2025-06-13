package com.karate.management.karatemanagementsystem.feedback.domain.service;

import com.karate.management.karatemanagementsystem.feedback.domain.exception.FeedbackNotFoundException;
import com.karate.management.karatemanagementsystem.feedback.domain.model.FeedbackEntity;
import com.karate.management.karatemanagementsystem.feedback.domain.repository.FeedbackRepository;
import com.karate.management.karatemanagementsystem.feedback.api.dto.FeedbackResponseDto;
import com.karate.management.karatemanagementsystem.training.domain.model.TrainingSessionEntity;
import com.karate.management.karatemanagementsystem.domain.user.UserEntity;
import com.karate.management.karatemanagementsystem.training.domain.repository.TrainingSessionRepository;
import com.karate.management.karatemanagementsystem.domain.user.UserRepository;
import com.karate.management.karatemanagementsystem.training.domain.exception.TrainingSessionNotFoundException;
import com.karate.management.karatemanagementsystem.domain.user.UserNotSignedUpException;
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
    private final UserRepository userRepository;
    private final TrainingSessionRepository trainingSessionRepository;

    @Transactional
    public FeedbackResponseDto getFeedbackForSession(Long sessionId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("User not authenticated");
        }

        String username = authentication.getName();
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        TrainingSessionEntity trainingSession = trainingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new TrainingSessionNotFoundException("Training session not found"));

        if (!user.getTrainingSessionEntities().contains(trainingSession)) {
            throw new UserNotSignedUpException("User is not enrolled in the specified training session");
        }

        FeedbackEntity feedback = feedbackRepository.findByTrainingSessionEntityTrainingSessionId(sessionId)
                .orElseThrow(() -> new FeedbackNotFoundException("Feedback not found for this session"));

        return new FeedbackResponseDto(feedback.getComment(), feedback.getStarRating());
    }
}

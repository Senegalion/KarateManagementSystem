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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FeedbackServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private TrainingSessionRepository trainingSessionRepository;

    @Mock
    private FeedbackRepository feedbackRepository;

    @InjectMocks
    private FeedbackService feedbackService;

    private UserEntity user;
    private TrainingSessionEntity session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new UserEntity();
        user.setUserId(1L);
        user.setUsername("testUser");

        session = new TrainingSessionEntity();
        session.setTrainingSessionId(1L);
        session.setDescription("Test Session");

        user.getTrainingSessionEntities().add(session);
    }

    @Test
    void should_add_feedback_for_user_on_session() {
        // given
        FeedbackRequestDto feedbackRequestDto = new FeedbackRequestDto("Great session!", 5);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(trainingSessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(feedbackRepository.save(any(FeedbackEntity.class))).thenReturn(new FeedbackEntity(1L, user, session, "Great session!", 5));

        // when
        FeedbackResponseDto feedbackResponse = feedbackService.addFeedbackToUserForTrainingSession(1L, 1L, feedbackRequestDto);

        // then
        assertNotNull(feedbackResponse);
        assertEquals("Great session!", feedbackResponse.comment());
        assertEquals(5, feedbackResponse.starRating());
        verify(feedbackRepository, times(1)).save(any(FeedbackEntity.class));
    }

    @Test
    void should_throw_exception_if_user_not_found() {
        // given
        FeedbackRequestDto feedbackRequestDto = new FeedbackRequestDto("Great session!", 5);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(UsernameNotFoundException.class, () -> feedbackService.addFeedbackToUserForTrainingSession(1L, 1L, feedbackRequestDto));
    }

    @Test
    void should_throw_exception_if_training_session_not_found() {
        // given
        FeedbackRequestDto feedbackRequestDto = new FeedbackRequestDto("Great session!", 5);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(trainingSessionRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(TrainingSessionNotFoundException.class, () -> feedbackService.addFeedbackToUserForTrainingSession(1L, 1L, feedbackRequestDto));
    }

    @Test
    void should_throw_exception_if_user_not_enrolled_in_session() {
        // given
        FeedbackRequestDto feedbackRequestDto = new FeedbackRequestDto("Great session!", 5);

        TrainingSessionEntity sessionNotEnrolled = new TrainingSessionEntity();
        sessionNotEnrolled.setTrainingSessionId(2L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(trainingSessionRepository.findById(2L)).thenReturn(Optional.of(sessionNotEnrolled));

        // when & then
        assertThrows(TrainingSessionNotFoundException.class, () -> feedbackService.addFeedbackToUserForTrainingSession(1L, 2L, feedbackRequestDto));
    }
}
package com.karate.management.karatemanagementsystem.service.user;

import com.karate.management.karatemanagementsystem.feedback.domain.service.UserFeedbackService;
import com.karate.management.karatemanagementsystem.feedback.api.dto.FeedbackResponseDto;
import com.karate.management.karatemanagementsystem.feedback.domain.model.FeedbackEntity;
import com.karate.management.karatemanagementsystem.training.domain.model.TrainingSessionEntity;
import com.karate.management.karatemanagementsystem.user.domain.model.UserEntity;
import com.karate.management.karatemanagementsystem.feedback.domain.repository.FeedbackRepository;
import com.karate.management.karatemanagementsystem.training.domain.repository.TrainingSessionRepository;
import com.karate.management.karatemanagementsystem.user.domain.repository.UserRepository;
import com.karate.management.karatemanagementsystem.feedback.domain.exception.FeedbackNotFoundException;
import com.karate.management.karatemanagementsystem.training.domain.exception.TrainingSessionNotFoundException;
import com.karate.management.karatemanagementsystem.user.domain.exception.UserNotSignedUpException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class UserFeedbackServiceTest {
    @Mock
    private FeedbackRepository feedbackRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TrainingSessionRepository trainingSessionRepository;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    private UserFeedbackService userFeedbackService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testUser");
        when(authentication.isAuthenticated()).thenReturn(true);
        userFeedbackService = new UserFeedbackService(feedbackRepository, userRepository, trainingSessionRepository);
    }

    @Test
    public void should_throw_username_not_found_exception_when_user_is_not_authenticated() {
        // given
        when(securityContext.getAuthentication()).thenReturn(null);

        // when & then
        assertThrows(UsernameNotFoundException.class, () -> userFeedbackService.getFeedbackForSession(1L));
    }

    @Test
    public void should_throw_username_not_found_exception_when_user_has_not_been_found() {
        // given
        when(userRepository.findByUsername("testUser")).thenReturn(java.util.Optional.empty());

        // when & then
        assertThrows(UsernameNotFoundException.class, () -> userFeedbackService.getFeedbackForSession(1L));
    }

    @Test
    public void should_throw_training_session_not_found_exception_when_training_session_has_not_been_found() {
        // given
        when(userRepository.findByUsername("testUser")).thenReturn(java.util.Optional.of(new UserEntity()));
        when(trainingSessionRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        // when & then
        assertThrows(TrainingSessionNotFoundException.class, () -> userFeedbackService.getFeedbackForSession(1L));
    }

    @Test
    public void should_throw_user_not_signed_up_exception_when_user_wants_to_get_feedback_from_session_on_which_user_has_not_enrolled() {
        // given
        UserEntity user = new UserEntity();
        TrainingSessionEntity trainingSession = new TrainingSessionEntity();

        when(userRepository.findByUsername("testUser")).thenReturn(java.util.Optional.of(user));
        when(trainingSessionRepository.findById(1L)).thenReturn(java.util.Optional.of(trainingSession));

        // when & then
        assertThrows(UserNotSignedUpException.class, () -> userFeedbackService.getFeedbackForSession(1L));
    }

    @Test
    public void should_throw_feedback_not_found_exception_when_user_wants_to_see_feedback_that_has_not_been_created_yet() {
        // given
        UserEntity user = new UserEntity();
        TrainingSessionEntity trainingSession = new TrainingSessionEntity();
        trainingSession.getUserEntities().add(user);
        user.getTrainingSessionEntities().add(trainingSession);

        when(userRepository.findByUsername("testUser")).thenReturn(java.util.Optional.of(user));
        when(trainingSessionRepository.findById(1L)).thenReturn(java.util.Optional.of(trainingSession));
        when(feedbackRepository.findByTrainingSessionEntityTrainingSessionId(1L)).thenReturn(java.util.Optional.empty());

        // when & then
        assertThrows(FeedbackNotFoundException.class, () -> userFeedbackService.getFeedbackForSession(1L));
    }

    @Test
    public void should_return_feedback_when_user_wants_to_see_feedback_for_training_session() {
        // given
        UserEntity user = new UserEntity();
        TrainingSessionEntity trainingSession = new TrainingSessionEntity();
        trainingSession.getUserEntities().add(user);
        user.getTrainingSessionEntities().add(trainingSession);

        FeedbackEntity feedbackEntity = new FeedbackEntity();
        feedbackEntity.setComment("Great session!");
        feedbackEntity.setStarRating(5);

        when(userRepository.findByUsername("testUser")).thenReturn(java.util.Optional.of(user));
        when(trainingSessionRepository.findById(1L)).thenReturn(java.util.Optional.of(trainingSession));
        when(feedbackRepository.findByTrainingSessionEntityTrainingSessionId(1L)).thenReturn(java.util.Optional.of(feedbackEntity));

        // when
        FeedbackResponseDto feedbackResponse = userFeedbackService.getFeedbackForSession(1L);

        // then
        assertNotNull(feedbackResponse);
        assertEquals("Great session!", feedbackResponse.comment());
        assertEquals(5, feedbackResponse.starRating());
    }
}
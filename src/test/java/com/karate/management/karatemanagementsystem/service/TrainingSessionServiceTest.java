package com.karate.management.karatemanagementsystem.service;

import com.karate.management.karatemanagementsystem.controller.rest.user.TrainingSessionRegistrationResponseDto;
import com.karate.management.karatemanagementsystem.model.dto.TrainingSessionDto;
import com.karate.management.karatemanagementsystem.model.entity.TrainingSessionEntity;
import com.karate.management.karatemanagementsystem.model.entity.UserEntity;
import com.karate.management.karatemanagementsystem.model.repository.TrainingSessionRepository;
import com.karate.management.karatemanagementsystem.model.repository.UserRepository;
import com.karate.management.karatemanagementsystem.service.exception.TrainingSessionNotFoundException;
import com.karate.management.karatemanagementsystem.service.exception.UserAlreadySignedUpException;
import com.karate.management.karatemanagementsystem.service.exception.UserNotSignedUpException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainingSessionServiceTest {
    @Mock
    private TrainingSessionRepository trainingSessionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private TrainingSessionService trainingSessionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testUser");
    }

    @Test
    void should_return_all_training_sessions_when_sessions_exist() {
        // given
        TrainingSessionEntity session1 = new TrainingSessionEntity();
        session1.setTrainingSessionId(1L);
        session1.setDescription("Session 1");
        session1.setDate(LocalDateTime.of(2025, 3, 18, 20, 0, 0));

        TrainingSessionEntity session2 = new TrainingSessionEntity();
        session2.setTrainingSessionId(2L);
        session2.setDescription("Session 2");
        session2.setDate(LocalDateTime.of(2025, 3, 20, 20, 0, 0));

        List<TrainingSessionEntity> sessions = List.of(session1, session2);
        when(trainingSessionRepository.findAll()).thenReturn(sessions);

        // when
        List<TrainingSessionDto> result = trainingSessionService.getAllTrainingSessions();

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Session 1", result.get(0).description());
        assertEquals("Session 2", result.get(1).description());
    }

    @Test
    void should_throw_training_session_not_found_exception_when_no_sessions_exist() {
        // given
        when(trainingSessionRepository.findAll()).thenReturn(List.of());

        // when & then
        TrainingSessionNotFoundException exception = assertThrows(TrainingSessionNotFoundException.class,
                () -> trainingSessionService.getAllTrainingSessions());
        assertEquals("No training sessions found", exception.getMessage());
    }

    @Test
    void should_handle_exception_when_connection_to_database_fails() {
        // given
        when(trainingSessionRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> trainingSessionService.getAllTrainingSessions());
        assertEquals("Database error", exception.getMessage());
    }

    @Test
    void should_sign_up_user_for_training_session() {
        // given
        UserEntity user = new UserEntity();
        user.setUsername("testUser");

        TrainingSessionEntity session = new TrainingSessionEntity();
        session.setTrainingSessionId(1L);
        session.setDescription("Karate Training");
        session.setDate(LocalDateTime.now());

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(trainingSessionRepository.findById(1L)).thenReturn(Optional.of(session));

        // when
        TrainingSessionRegistrationResponseDto response = trainingSessionService.signUpForTrainingSession(1L);

        // then
        assertNotNull(response);
        assertEquals("Successfully signed up for the training session", response.message());
        verify(userRepository, times(1)).save(user);
        verify(trainingSessionRepository, times(1)).save(session);
    }

    @Test
    void should_throw_exception_when_user_already_signed_up() {
        // given
        UserEntity user = new UserEntity();
        user.setUsername("testUser");
        TrainingSessionEntity session = new TrainingSessionEntity();
        session.setTrainingSessionId(1L);
        session.setDescription("Karate Training");
        session.setDate(LocalDateTime.now());
        user.getTrainingSessionEntities().add(session);
        session.getUserEntities().add(user);

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(trainingSessionRepository.findById(1L)).thenReturn(Optional.of(session));

        // when & then
        assertThrows(UserAlreadySignedUpException.class, () -> trainingSessionService.signUpForTrainingSession(1L));
    }

    @Test
    void should_withdraw_user_from_training_session() {
        // given
        UserEntity user = new UserEntity();
        user.setUsername("testUser");
        TrainingSessionEntity session = new TrainingSessionEntity();
        session.setTrainingSessionId(1L);
        session.setDate(LocalDateTime.now());
        user.getTrainingSessionEntities().add(session);
        session.getUserEntities().add(user);

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(trainingSessionRepository.findById(1L)).thenReturn(Optional.of(session));

        // when
        TrainingSessionRegistrationResponseDto response = trainingSessionService.withdrawFromTrainingSession(1L);

        // then
        assertNotNull(response);
        assertEquals("Successfully withdrawn from the training session", response.message());
        verify(userRepository, times(1)).save(user);
        verify(trainingSessionRepository, times(1)).save(session);
    }

    @Test
    void should_throw_exception_when_user_not_signed_up() {
        // given
        UserEntity user = new UserEntity();
        user.setUsername("testUser");
        TrainingSessionEntity session = new TrainingSessionEntity();
        session.setTrainingSessionId(1L);

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(trainingSessionRepository.findById(1L)).thenReturn(Optional.of(session));

        // when & then
        assertThrows(UserNotSignedUpException.class, () -> trainingSessionService.withdrawFromTrainingSession(1L));
    }
}
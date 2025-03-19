package com.karate.management.karatemanagementsystem.service;

import com.karate.management.karatemanagementsystem.model.dto.TrainingSessionDto;
import com.karate.management.karatemanagementsystem.model.entity.TrainingSessionEntity;
import com.karate.management.karatemanagementsystem.model.repository.TrainingSessionRepository;
import com.karate.management.karatemanagementsystem.service.exception.TrainingSessionNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class TrainingSessionServiceTest {
    @Mock
    private TrainingSessionRepository trainingSessionRepository;

    @InjectMocks
    private TrainingSessionService trainingSessionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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
}
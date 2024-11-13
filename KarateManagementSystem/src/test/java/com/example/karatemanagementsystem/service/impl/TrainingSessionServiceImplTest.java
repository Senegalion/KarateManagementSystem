package com.example.karatemanagementsystem.service.impl;

import com.example.karatemanagementsystem.model.TrainingSession;
import com.example.karatemanagementsystem.model.User;
import com.example.karatemanagementsystem.repository.TrainingSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainingSessionServiceImplTest {

    @Mock
    private TrainingSessionRepository trainingSessionRepository;

    @InjectMocks
    private TrainingSessionServiceImpl trainingSessionService;

    private TrainingSession trainingSession;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        trainingSession = new TrainingSession();
        trainingSession.setId(1L);
    }

    @Test
    void getTrainingSessionById_ShouldReturnTrainingSession_WhenSessionExists() {
        when(trainingSessionRepository.findById(1L)).thenReturn(Optional.of(trainingSession));

        Optional<TrainingSession> foundSession = trainingSessionService.getTrainingSessionById(1L);

        assertTrue(foundSession.isPresent());
        assertEquals(1L, foundSession.get().getId());
        verify(trainingSessionRepository, times(1)).findById(1L);
    }

    @Test
    void getTrainingSessionById_ShouldReturnEmpty_WhenSessionDoesNotExist() {
        when(trainingSessionRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<TrainingSession> foundSession = trainingSessionService.getTrainingSessionById(1L);

        assertFalse(foundSession.isPresent());
        verify(trainingSessionRepository, times(1)).findById(1L);
    }

    @Test
    void findTrainingSessionsByUser_ShouldReturnSessions() {
        User user = new User();
        user.setId(1L);
        when(trainingSessionRepository.findByUsers(user)).thenReturn(List.of(trainingSession));

        List<TrainingSession> sessions = trainingSessionService.findTrainingSessionsByUser(user);

        assertEquals(1, sessions.size());
        assertEquals(1L, sessions.get(0).getId());
        verify(trainingSessionRepository, times(1)).findByUsers(user);
    }
}
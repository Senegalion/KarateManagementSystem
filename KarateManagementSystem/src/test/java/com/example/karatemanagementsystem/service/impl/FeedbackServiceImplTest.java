package com.example.karatemanagementsystem.service.impl;

import com.example.karatemanagementsystem.model.Feedback;
import com.example.karatemanagementsystem.model.TrainingSession;
import com.example.karatemanagementsystem.model.User;
import com.example.karatemanagementsystem.repository.FeedbackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class FeedbackServiceImplTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @InjectMocks
    private FeedbackServiceImpl feedbackService;

    private Feedback feedback;
    private TrainingSession session;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        feedback = new Feedback();
        feedback.setId(1L);
        session = new TrainingSession();
        session.setId(1L);
        user = new User();
        user.setId(1L);
    }

    @Test
    void getFeedbackByTrainingSession_ShouldReturnFeedbackList() {
        when(feedbackRepository.findByTrainingSession(session)).thenReturn(List.of(feedback));

        List<Feedback> feedbacks = feedbackService.getFeedbackByTrainingSession(session);

        assertEquals(1, feedbacks.size());
        assertEquals(1L, feedbacks.get(0).getId());
        verify(feedbackRepository, times(1)).findByTrainingSession(session);
    }

    @Test
    void getFeedbackByUser_ShouldReturnFeedbackList() {
        when(feedbackRepository.findByUser(user)).thenReturn(List.of(feedback));

        List<Feedback> feedbacks = feedbackService.getFeedbackByUser(user);

        assertEquals(1, feedbacks.size());
        assertEquals(1L, feedbacks.get(0).getId());
        verify(feedbackRepository, times(1)).findByUser(user);
    }

    @Test
    void saveFeedback_ShouldSaveFeedback() {
        feedbackService.saveFeedback(feedback);
        verify(feedbackRepository, times(1)).save(feedback);
    }

    @Test
    void deleteFeedback_ShouldDeleteFeedback() {
        feedbackService.deleteFeedback(feedback);
        verify(feedbackRepository, times(1)).delete(feedback);
    }
}
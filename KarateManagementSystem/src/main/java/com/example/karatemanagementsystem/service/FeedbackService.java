package com.example.karatemanagementsystem.service;

import com.example.karatemanagementsystem.model.Feedback;
import com.example.karatemanagementsystem.model.TrainingSession;
import com.example.karatemanagementsystem.model.User;

import java.util.List;

public interface FeedbackService {
    List<Feedback> getFeedbackByTrainingSession(TrainingSession session);
    List<Feedback> getFeedbackByUser(User user);
    void saveFeedback(Feedback feedback);
    void deleteFeedback(Feedback feedback);
    void deleteAllFeedbacks(List<Feedback> feedbacks);
}

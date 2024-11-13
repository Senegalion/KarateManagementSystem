package com.example.karatemanagementsystem.service.impl;

import com.example.karatemanagementsystem.model.Feedback;
import com.example.karatemanagementsystem.model.TrainingSession;
import com.example.karatemanagementsystem.model.User;
import com.example.karatemanagementsystem.repository.FeedbackRepository;
import com.example.karatemanagementsystem.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;

    @Autowired
    public FeedbackServiceImpl(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    @Override
    public List<Feedback> getFeedbackByTrainingSession(TrainingSession session) {
        return feedbackRepository.findByTrainingSession(session);
    }

    @Override
    public List<Feedback> getFeedbackByUser(User user) {
        return feedbackRepository.findByUser(user);
    }

    @Override
    public void saveFeedback(Feedback feedback) {
        feedbackRepository.save(feedback);
    }

    @Override
    public void deleteFeedback(Feedback feedback) {
        feedbackRepository.delete(feedback);
    }

    @Override
    public void deleteAllFeedbacks(List<Feedback> feedbacks) {
        feedbackRepository.deleteAll(feedbacks);
    }
}
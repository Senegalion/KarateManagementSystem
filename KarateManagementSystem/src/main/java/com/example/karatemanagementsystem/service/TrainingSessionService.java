package com.example.karatemanagementsystem.service;

import com.example.karatemanagementsystem.model.TrainingSession;
import com.example.karatemanagementsystem.model.User;

import java.util.List;
import java.util.Optional;

public interface TrainingSessionService {
    List<TrainingSession> getAllTrainingSessions();
    Optional<TrainingSession> getTrainingSessionById(long id);
    void saveTrainingSession(TrainingSession session);
    void deleteTrainingSession(long id);
    List<TrainingSession> findTrainingSessionsByUser(User user);
}

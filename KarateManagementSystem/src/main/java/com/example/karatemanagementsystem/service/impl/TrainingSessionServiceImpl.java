package com.example.karatemanagementsystem.service.impl;

import com.example.karatemanagementsystem.model.TrainingSession;
import com.example.karatemanagementsystem.model.User;
import com.example.karatemanagementsystem.repository.TrainingSessionRepository;
import com.example.karatemanagementsystem.service.TrainingSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrainingSessionServiceImpl implements TrainingSessionService {

    private final TrainingSessionRepository trainingSessionRepository;

    @Autowired
    public TrainingSessionServiceImpl(TrainingSessionRepository trainingSessionRepository) {
        this.trainingSessionRepository = trainingSessionRepository;
    }

    @Override
    public List<TrainingSession> getAllTrainingSessions() {
        return trainingSessionRepository.findAll();
    }

    @Override
    public Optional<TrainingSession> getTrainingSessionById(long id) {
        return trainingSessionRepository.findById(id);
    }

    @Override
    public void saveTrainingSession(TrainingSession session) {
        trainingSessionRepository.save(session);
    }

    @Override
    public void deleteTrainingSession(long id) {
        trainingSessionRepository.deleteById(id);
    }

    @Override
    public List<TrainingSession> findTrainingSessionsByUser(User user) {
        return trainingSessionRepository.findByUsers(user);
    }
}

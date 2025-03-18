package com.karate.management.karatemanagementsystem.service;

import com.karate.management.karatemanagementsystem.model.dto.TrainingSessionDto;
import com.karate.management.karatemanagementsystem.model.entity.TrainingSessionEntity;
import com.karate.management.karatemanagementsystem.model.repository.TrainingSessionRepository;
import com.karate.management.karatemanagementsystem.service.exception.TrainingSessionNotFoundException;
import com.karate.management.karatemanagementsystem.service.mapper.TrainingSessionMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class TrainingSessionService {
    private final TrainingSessionRepository trainingSessionRepository;

    public List<TrainingSessionDto> getAllTrainingSessions() {
        List<TrainingSessionEntity> trainingSessions = trainingSessionRepository.findAll();
        if (trainingSessions.isEmpty()) {
            throw new TrainingSessionNotFoundException("No training sessions found");
        }
        return trainingSessions.stream()
                .map(TrainingSessionMapper::mapToTrainingSessionDto)
                .toList();
    }
}

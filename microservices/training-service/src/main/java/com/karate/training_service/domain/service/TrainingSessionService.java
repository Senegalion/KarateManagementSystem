package com.karate.training_service.domain.service;

import com.karate.training_service.api.dto.TrainingSessionDto;
import com.karate.training_service.api.dto.TrainingSessionRequestDto;
import com.karate.training_service.domain.model.TrainingSessionEntity;
import com.karate.training_service.domain.repository.TrainingSessionRepository;
import com.karate.training_service.infrastructure.client.UserServiceClient;
import com.karate.training_service.infrastructure.persistence.mapper.TrainingSessionMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class TrainingSessionService {
    private final TrainingSessionRepository trainingSessionRepository;
    private final UserServiceClient userServiceClient;

    public List<TrainingSessionDto> getAllTrainingSessionsForCurrentUserClub() {
        Long clubId = userServiceClient.getCurrentUserClubId();
        List<TrainingSessionEntity> trainingSessions = trainingSessionRepository.findAllByClubId(clubId);

        return trainingSessions.stream()
                .map(TrainingSessionMapper::mapToTrainingSessionDto)
                .toList();
    }

    @Transactional
    public TrainingSessionDto createTrainingSession(TrainingSessionRequestDto dto) {
        Long clubId = userServiceClient.getCurrentUserClubId();

        TrainingSessionEntity trainingSession = new TrainingSessionEntity();
        trainingSession.setDate(dto.date());
        trainingSession.setDescription(dto.description());
        trainingSession.setClubId(clubId);

        TrainingSessionEntity saved = trainingSessionRepository.save(trainingSession);

        return TrainingSessionMapper.mapToTrainingSessionDto(saved);
    }
}

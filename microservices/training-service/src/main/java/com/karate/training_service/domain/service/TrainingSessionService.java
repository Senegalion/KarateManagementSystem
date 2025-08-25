package com.karate.training_service.domain.service;

import com.karate.training_service.api.dto.TrainingSessionDto;
import com.karate.training_service.api.dto.TrainingSessionRequestDto;
import com.karate.training_service.domain.exception.TrainingSessionClubMismatchException;
import com.karate.training_service.domain.exception.TrainingSessionNotFoundException;
import com.karate.training_service.domain.model.TrainingSessionEntity;
import com.karate.training_service.domain.repository.TrainingSessionRepository;
import com.karate.training_service.infrastructure.client.UserServiceClient;
import com.karate.training_service.infrastructure.persistence.mapper.TrainingSessionMapper;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class TrainingSessionService {
    private final TrainingSessionRepository trainingSessionRepository;
    private final UserServiceClient userServiceClient;

    public List<TrainingSessionDto> getAllTrainingSessionsForCurrentUserClub() {
        String username = getCurrentUsername();
        Long clubId = userServiceClient.getUserClubId(username);
        List<TrainingSessionEntity> trainingSessions = trainingSessionRepository.findAllByClubId(clubId);

        return trainingSessions.stream()
                .map(TrainingSessionMapper::mapToTrainingSessionDto)
                .toList();
    }

    @Transactional
    public TrainingSessionDto createTrainingSession(TrainingSessionRequestDto dto) {
        if (dto.endTime().isBefore(dto.startTime()) || dto.endTime().isEqual(dto.startTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        String username = getCurrentUsername();
        Long clubId = userServiceClient.getUserClubId(username);

        TrainingSessionEntity trainingSession = new TrainingSessionEntity();
        trainingSession.setStartTime(dto.startTime());
        trainingSession.setEndTime(dto.endTime());
        trainingSession.setDescription(dto.description());
        trainingSession.setClubId(clubId);

        TrainingSessionEntity saved = trainingSessionRepository.save(trainingSession);

        return TrainingSessionMapper.mapToTrainingSessionDto(saved);
    }

    @Transactional
    public void deleteTrainingSession(Long trainingId) {
        String username = getCurrentUsername();
        Long clubId = userServiceClient.getUserClubId(username);

        TrainingSessionEntity training = trainingSessionRepository.findById(trainingId)
                .orElseThrow(() -> new TrainingSessionNotFoundException("Training session not found"));

        if (!training.getClubId().equals(clubId)) {
            throw new TrainingSessionClubMismatchException("You cannot delete a training from another club");
        }

        trainingSessionRepository.delete(training);
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("No authenticated user found");
        }
        return authentication.getName();
    }

    public Boolean checkTrainingExists(Long trainingId) {
        return trainingSessionRepository.existsById(trainingId);
    }

    @Transactional(readOnly = true)
    public TrainingSessionDto getTrainingById(Long trainingId) {
        TrainingSessionEntity trainingSessionEntity = trainingSessionRepository.findById(trainingId)
                .orElseThrow(() -> new RuntimeException("Training Session not found"));

        return new TrainingSessionDto(
                trainingSessionEntity.getTrainingSessionId(),
                trainingSessionEntity.getStartTime(),
                trainingSessionEntity.getEndTime(),
                trainingSessionEntity.getDescription()
        );
    }
}

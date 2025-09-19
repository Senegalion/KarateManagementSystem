package com.karate.training_service.domain.service;

import com.karate.training_service.api.dto.TrainingSessionDto;
import com.karate.training_service.api.dto.TrainingSessionRequestDto;
import com.karate.training_service.domain.exception.AuthenticationMissingException;
import com.karate.training_service.domain.exception.InvalidTrainingTimeRangeException;
import com.karate.training_service.domain.exception.TrainingSessionClubMismatchException;
import com.karate.training_service.domain.exception.TrainingSessionNotFoundException;
import com.karate.training_service.domain.model.TrainingSessionEntity;
import com.karate.training_service.domain.repository.TrainingSessionRepository;
import com.karate.training_service.infrastructure.persistence.mapper.TrainingSessionMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class TrainingSessionService {
    private final TrainingSessionRepository trainingSessionRepository;
    private final UpstreamGateway upstream;

    public List<TrainingSessionDto> getAllTrainingSessionsForCurrentUserClub() {
        String username = getCurrentUsername();
        long t0 = System.currentTimeMillis();
        Long clubId = upstream.getUserClubId(username);
        log.debug("user-service getUserClubId username='{}' -> clubId={} took={}ms",
                username, clubId, System.currentTimeMillis() - t0);

        List<TrainingSessionEntity> trainingSessions = trainingSessionRepository.findAllByClubId(clubId);
        log.info("Fetched {} trainings for clubId={}", trainingSessions.size(), clubId);

        return trainingSessions.stream()
                .map(TrainingSessionMapper::mapToTrainingSessionDto)
                .toList();
    }

    @Transactional
    public TrainingSessionDto createTrainingSession(TrainingSessionRequestDto dto) {
        log.info("Create training startTime={} endTime={} desc='{}'",
                dto.startTime(), dto.endTime(), dto.description());

        if (dto.endTime().isBefore(dto.startTime()) || dto.endTime().isEqual(dto.startTime())) {
            log.warn("Invalid time range: start={} end={}", dto.startTime(), dto.endTime());
            throw new InvalidTrainingTimeRangeException("End time must be after start time");
        }

        String username = getCurrentUsername();
        long t0 = System.currentTimeMillis();
        Long clubId = upstream.getUserClubId(username);
        log.debug("Resolved clubId={} for username='{}' took={}ms",
                clubId, username, System.currentTimeMillis() - t0);

        TrainingSessionEntity trainingSession = new TrainingSessionEntity();
        trainingSession.setStartTime(dto.startTime());
        trainingSession.setEndTime(dto.endTime());
        trainingSession.setDescription(dto.description());
        trainingSession.setClubId(clubId);

        long tDb = System.currentTimeMillis();
        TrainingSessionEntity saved = trainingSessionRepository.save(trainingSession);
        log.info("Training persisted trainingId={} took={}ms",
                saved.getTrainingSessionId(), System.currentTimeMillis() - tDb);

        return TrainingSessionMapper.mapToTrainingSessionDto(saved);
    }

    @Transactional
    public void deleteTrainingSession(Long trainingId) {
        log.info("Delete training trainingId={}", trainingId);
        String username = getCurrentUsername();
        Long clubId = upstream.getUserClubId(username);

        TrainingSessionEntity training = trainingSessionRepository.findById(trainingId)
                .orElseThrow(() -> {
                    log.warn("Training not found trainingId={}", trainingId);
                    return new TrainingSessionNotFoundException("Training session not found");
                });

        if (!training.getClubId().equals(clubId)) {
            log.warn("Club mismatch trainingId={} trainingClubId={} userClubId={}",
                    trainingId, training.getClubId(), clubId);
            throw new TrainingSessionClubMismatchException("You cannot delete a training from another club");
        }

        trainingSessionRepository.delete(training);
        log.info("Delete training OK trainingId={}", trainingId);
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            log.error("No authenticated user found in SecurityContext");
            throw new AuthenticationMissingException("No authenticated user found");
        }
        return authentication.getName();
    }

    public Boolean checkTrainingExists(Long trainingId) {
        boolean exists = trainingSessionRepository.existsById(trainingId);
        log.debug("checkTrainingExists trainingId={} -> {}", trainingId, exists);
        return exists;
    }

    @Transactional(readOnly = true)
    public TrainingSessionDto getTrainingById(Long trainingId) {
        log.debug("getTrainingById trainingId={}", trainingId);
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

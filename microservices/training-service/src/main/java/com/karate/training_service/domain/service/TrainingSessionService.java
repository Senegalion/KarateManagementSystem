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
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
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
    private final CacheManager cacheManager;

    public Long currentUserClubId() {
        String username = getCurrentUsername();
        return upstream.getUserClubId(username);
    }

    @Cacheable(cacheNames = "trainingsByClub", key = "#root.target.currentUserClubId()")
    public List<TrainingSessionDto> getAllTrainingSessionsForCurrentUserClub() {
        Long clubId = currentUserClubId();
        long tDb = System.currentTimeMillis();
        List<TrainingSessionEntity> trainingSessions = trainingSessionRepository.findAllByClubId(clubId);
        log.info("Fetched {} trainings for clubId={} took={}ms",
                trainingSessions.size(), clubId, System.currentTimeMillis() - tDb);

        return trainingSessions.stream()
                .map(TrainingSessionMapper::mapToTrainingSessionDto)
                .toList();
    }

    @Transactional
    public TrainingSessionDto createTrainingSession(TrainingSessionRequestDto dto) {
        log.info("Create training startTime={} endTime={} desc='{}'",
                dto.startTime(), dto.endTime(), dto.description());

        if (!dto.endTime().isAfter(dto.startTime())) {
            throw new InvalidTrainingTimeRangeException("End time must be after start time");
        }

        Long clubId = currentUserClubId();

        TrainingSessionEntity trainingSession = new TrainingSessionEntity();
        trainingSession.setStartTime(dto.startTime());
        trainingSession.setEndTime(dto.endTime());
        trainingSession.setDescription(dto.description());
        trainingSession.setClubId(clubId);

        TrainingSessionEntity saved = trainingSessionRepository.save(trainingSession);
        TrainingSessionDto result = TrainingSessionMapper.mapToTrainingSessionDto(saved);

        // precyzyjne czyszczenie cache
        evictTrainingsByClub(clubId);
        evictTrainingById(saved.getTrainingSessionId());
        evictTrainingExists(saved.getTrainingSessionId());

        return result;
    }

    @Transactional
    public void deleteTrainingSession(Long trainingId) {
        log.info("Delete training trainingId={}", trainingId);
        Long userClubId = currentUserClubId();

        TrainingSessionEntity training = trainingSessionRepository.findById(trainingId)
                .orElseThrow(() -> new TrainingSessionNotFoundException("Training session not found"));

        if (!training.getClubId().equals(userClubId)) {
            throw new TrainingSessionClubMismatchException("You cannot delete a training from another club");
        }

        trainingSessionRepository.delete(training);

        evictTrainingById(trainingId);
        evictTrainingExists(trainingId);
        evictTrainingsByClub(userClubId);
    }

    @Cacheable(cacheNames = "trainingExists", key = "#trainingId")
    public Boolean checkTrainingExists(Long trainingId) {
        boolean exists = trainingSessionRepository.existsById(trainingId);
        log.debug("checkTrainingExists trainingId={} -> {}", trainingId, exists);
        return exists;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "trainingById", key = "#trainingId")
    public TrainingSessionDto getTrainingById(Long trainingId) {
        TrainingSessionEntity e = trainingSessionRepository.findById(trainingId)
                .orElseThrow(() -> new TrainingSessionNotFoundException("Training Session not found"));

        return new TrainingSessionDto(
                e.getTrainingSessionId(),
                e.getStartTime(),
                e.getEndTime(),
                e.getDescription()
        );
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) throw new AuthenticationMissingException("No authenticated user found");
        return authentication.getName();
    }

    private void evictTrainingsByClub(Long clubId) {
        Cache c = cacheManager.getCache("trainingsByClub");
        if (c != null) c.evictIfPresent(clubId);
    }

    private void evictTrainingById(Long trainingId) {
        Cache c = cacheManager.getCache("trainingById");
        if (c != null) c.evictIfPresent(trainingId);
    }

    private void evictTrainingExists(Long trainingId) {
        Cache c = cacheManager.getCache("trainingExists");
        if (c != null) c.evictIfPresent(trainingId);
    }
}

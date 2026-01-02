package com.karate.training_service.domain.service;

import com.karate.training_service.api.dto.TrainingRecurringRequestDto;
import com.karate.training_service.api.dto.TrainingSessionDto;
import com.karate.training_service.api.dto.TrainingSessionRequestDto;
import com.karate.training_service.domain.exception.*;
import com.karate.training_service.domain.model.TrainingSessionEntity;
import com.karate.training_service.domain.repository.TrainingSessionRepository;
import com.karate.training_service.infrastructure.messaging.TrainingEventProducer;
import com.karate.training_service.infrastructure.messaging.event.TrainingDeletedEvent;
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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class TrainingSessionService {
    private final TrainingSessionRepository trainingSessionRepository;
    private final UpstreamGateway upstream;
    private final CacheManager cacheManager;
    private final TrainingEventProducer trainingEventProducer;

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

        evictTrainingsByClub(clubId);
        evictTrainingById(saved.getTrainingSessionId());
        evictTrainingExists(saved.getTrainingSessionId());

        afterCommit(() -> {
            var event = new com.karate.training_service.infrastructure.messaging.event.TrainingCreatedEvent(
                    UUID.randomUUID().toString(),
                    "TRAINING_CREATED",
                    Instant.now(),
                    saved.getTrainingSessionId(),
                    saved.getClubId(),
                    saved.getStartTime(),
                    saved.getEndTime(),
                    saved.getDescription()
            );
            trainingEventProducer.sendTrainingCreatedEvent(event);
        });

        return result;
    }

    @Transactional
    public void deleteTrainingSession(Long trainingId) {
        log.info("Delete training trainingId={}", trainingId);
        Long userClubId = currentUserClubId();

        TrainingSessionEntity training = trainingSessionRepository.findById(trainingId)
                .orElseThrow(() -> new TrainingSessionNotFoundException("Training session not found"));

        if (!training.getClubId().equals(userClubId)) {
            log.warn("Club mismatch trainingId={} trainingClubId={} userClubId={}", trainingId, training.getClubId(), userClubId);
            throw new TrainingSessionClubMismatchException("You cannot delete a training from another club");
        }

        LocalDateTime now = LocalDateTime.now();

        if (training.getEndTime() != null) {
            if (!training.getEndTime().isAfter(now)) {
                log.warn("Attempt to delete past training trainingId={} endTime={}", trainingId, training.getEndTime());
                throw new TrainingAlreadyOccurredException("Cannot delete a training session that already finished");
            }
        } else {
            if (!training.getStartTime().isAfter(now)) {
                log.warn("Attempt to delete past/ongoing training (no endTime) trainingId={} startTime={}",
                        trainingId, training.getStartTime());
                throw new TrainingAlreadyOccurredException("Cannot delete a training session that already started");
            }
        }

        Long clubId = training.getClubId();

        trainingSessionRepository.delete(training);
        log.info("Training deleted trainingId={} clubId={}", trainingId, clubId);

        TrainingDeletedEvent event = new TrainingDeletedEvent(
                UUID.randomUUID().toString(),
                "TrainingDeleted",
                Instant.now(),
                trainingId,
                clubId
        );
        afterCommit(() -> trainingEventProducer.sendTrainingDeletedEvent(event));

        evictTrainingById(trainingId);
        evictTrainingExists(trainingId);
        evictTrainingsByClub(userClubId);

        log.info("Delete training OK trainingId={}", trainingId);
    }

    @Transactional
    public List<TrainingSessionDto> createRecurringTrainings(TrainingRecurringRequestDto dto) {
        if (!dto.toDate().isAfter(dto.fromDate()) && !dto.toDate().isEqual(dto.fromDate())) {
            throw new InvalidTrainingTimeRangeException("toDate must be >= fromDate");
        }
        if (!dto.endTime().isAfter(dto.startTime())) {
            throw new InvalidTrainingTimeRangeException("End time must be after start time");
        }

        Long clubId = currentUserClubId();

        var start = dto.fromDate();
        var end = dto.toDate();

        var toSave = new java.util.ArrayList<TrainingSessionEntity>();

        for (var d = start; !d.isAfter(end); d = d.plusDays(1)) {
            if (!dto.daysOfWeek().contains(d.getDayOfWeek())) continue;

            var startDT = d.atTime(dto.startTime());
            var endDT = d.atTime(dto.endTime());

            boolean exists = trainingSessionRepository.existsByClubIdAndStartTime(clubId, startDT);
            if (exists) {
                if (dto.skipConflicts()) continue;
                throw new TrainingConflictException("Training already exists for: " + startDT);
            }

            TrainingSessionEntity e = new TrainingSessionEntity();
            e.setClubId(clubId);
            e.setStartTime(startDT);
            e.setEndTime(endDT);
            e.setDescription(dto.description());

            toSave.add(e);
        }

        var saved = trainingSessionRepository.saveAll(toSave);

        evictTrainingsByClub(clubId);

        afterCommit(() -> {
            for (TrainingSessionEntity s : saved) {
                var event = new com.karate.training_service.infrastructure.messaging.event.TrainingCreatedEvent(
                        UUID.randomUUID().toString(),
                        "TRAINING_CREATED",
                        Instant.now(),
                        s.getTrainingSessionId(),
                        s.getClubId(),
                        s.getStartTime(),
                        s.getEndTime(),
                        s.getDescription()
                );
                trainingEventProducer.sendTrainingCreatedEvent(event);
            }
        });

        return saved.stream()
                .map(TrainingSessionMapper::mapToTrainingSessionDto)
                .toList();
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

    private void afterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }
}

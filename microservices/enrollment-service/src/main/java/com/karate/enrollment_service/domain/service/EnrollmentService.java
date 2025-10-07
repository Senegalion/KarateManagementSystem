package com.karate.enrollment_service.domain.service;

import com.karate.enrollment_service.api.dto.EnrollmentDto;
import com.karate.enrollment_service.domain.exception.TrainingNotFoundException;
import com.karate.enrollment_service.domain.exception.UserAlreadyEnrolledException;
import com.karate.enrollment_service.domain.exception.UserNotEnrolledException;
import com.karate.enrollment_service.domain.exception.UserNotFoundException;
import com.karate.enrollment_service.domain.mapper.EnrollmentMapper;
import com.karate.enrollment_service.domain.model.EnrollmentEntity;
import com.karate.enrollment_service.domain.repository.EnrollmentRepository;
import com.karate.enrollment_service.infrastructure.messaging.EnrollmentEventProducer;
import com.karate.enrollment_service.infrastructure.messaging.event.EnrollmentEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentMapper enrollmentMapper;
    private final EnrollmentEventProducer eventProducer;
    private final UpstreamGateway upstream;

    @Transactional
    public EnrollmentDto enrollUser(Long userId, Long trainingId) {
        long t0 = System.currentTimeMillis();
        log.info("Enroll start userId={} trainingId={}", userId, trainingId);

        if (Boolean.FALSE.equals(upstream.checkUserExists(userId))) {
            throw new UserNotFoundException("User with id " + userId + " does not exist");
        }

        if (Boolean.FALSE.equals(upstream.checkTrainingSessionExists(trainingId))) {
            throw new TrainingNotFoundException("Training with id " + trainingId + " does not exist");
        }

        var user = upstream.getUser(userId);

        var training = upstream.getTrainingById(trainingId);

        enrollmentRepository.findByUserIdAndTrainingId(userId, trainingId)
                .ifPresent(e -> {
                    throw new UserAlreadyEnrolledException("User already enrolled for this training");
                });

        EnrollmentEntity enrollment = EnrollmentEntity.builder()
                .userId(userId)
                .trainingId(trainingId)
                .enrolledAt(LocalDateTime.now())
                .build();

        EnrollmentEntity saved = enrollmentRepository.save(enrollment);

        EnrollmentEvent event = new EnrollmentEvent(
                UUID.randomUUID().toString(),
                "USER_ENROLLED",
                Instant.now(),
                new EnrollmentEvent.Payload(
                        user.userId(),
                        user.userEmail(),
                        user.username(),
                        training.trainingSessionId(),
                        training.description(),
                        training.startTime(),
                        training.endTime()
                )
        );
        eventProducer.sendEnrollmentEvent(event);

        log.info("Enroll OK userId={} trainingId={} took={}ms", userId, trainingId, System.currentTimeMillis() - t0);
        return enrollmentMapper.toDto(saved);
    }

    @Transactional
    public void withdrawUser(Long userId, Long trainingId) {
        long t0 = System.currentTimeMillis();
        log.info("Withdraw start userId={} trainingId={}", userId, trainingId);

        EnrollmentEntity enrollment = enrollmentRepository.findByUserIdAndTrainingId(userId, trainingId)
                .orElseThrow(() -> new UserNotEnrolledException("User is not enrolled for this training"));

        var user = upstream.getUser(userId);
        var training = upstream.getTrainingById(trainingId);

        enrollmentRepository.delete(enrollment);

        EnrollmentEvent event = new EnrollmentEvent(
                UUID.randomUUID().toString(),
                "USER_UNENROLLED",
                Instant.now(),
                new EnrollmentEvent.Payload(
                        user.userId(),
                        user.userEmail(),
                        user.username(),
                        training.trainingSessionId(),
                        training.description(),
                        training.startTime(),
                        training.endTime()
                )
        );
        eventProducer.sendEnrollmentEvent(event);
        log.info("Withdraw OK userId={} trainingId={} took={}ms", userId, trainingId, System.currentTimeMillis() - t0);
    }

    public List<EnrollmentDto> getUserEnrollments(Long userId) {
        log.debug("getUserEnrollments userId={}", userId);
        return enrollmentRepository.findAllByUserId(userId).stream()
                .map(enrollmentMapper::toDto)
                .toList();
    }

    public List<EnrollmentDto> getTrainingEnrollments(Long trainingId) {
        log.debug("getTrainingEnrollments trainingId={}", trainingId);
        return enrollmentRepository.findAllByTrainingId(trainingId).stream()
                .map(enrollmentMapper::toDto)
                .toList();
    }

    public boolean isUserEnrolledInSession(Long userId, Long trainingId) {
        boolean present = enrollmentRepository.findByUserIdAndTrainingId(userId, trainingId).isPresent();
        log.debug("isUserEnrolledInSession userId={} trainingId={} -> {}", userId, trainingId, present);
        return present;
    }

    public Long resolveUserId(Authentication authentication) {
        String username = authentication.getName();
        long t0 = System.currentTimeMillis();
        Long id = upstream.getUserIdByUsername(username);
        log.debug("auth-service getUserIdByUsername username='{}' -> {} took={}ms", username, id, System.currentTimeMillis() - t0);
        return id;
    }
}

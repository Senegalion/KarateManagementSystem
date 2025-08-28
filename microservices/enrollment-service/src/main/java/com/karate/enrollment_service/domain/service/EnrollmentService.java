package com.karate.enrollment_service.domain.service;

import com.karate.enrollment_service.api.dto.EnrollmentDto;
import com.karate.enrollment_service.domain.exception.TrainingNotFoundException;
import com.karate.enrollment_service.domain.exception.UserAlreadyEnrolledException;
import com.karate.enrollment_service.domain.exception.UserNotEnrolledException;
import com.karate.enrollment_service.domain.exception.UserNotFoundException;
import com.karate.enrollment_service.domain.mapper.EnrollmentMapper;
import com.karate.enrollment_service.domain.model.EnrollmentEntity;
import com.karate.enrollment_service.domain.repository.EnrollmentRepository;
import com.karate.enrollment_service.infrastructure.client.TrainingClient;
import com.karate.enrollment_service.infrastructure.client.UserClient;
import com.karate.enrollment_service.infrastructure.messaging.EnrollmentEventProducer;
import com.karate.enrollment_service.infrastructure.messaging.event.EnrollmentEvent;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserClient userClient;
    private final TrainingClient trainingClient;
    private final EnrollmentMapper enrollmentMapper;
    private final EnrollmentEventProducer eventProducer;

    @Transactional
    public EnrollmentDto enrollUser(Long userId, Long trainingId) {
        if (Boolean.FALSE.equals(userClient.checkUserExists(userId))) {
            throw new UserNotFoundException("User with id " + userId + " does not exist");
        }

        if (Boolean.FALSE.equals(trainingClient.checkTrainingSessionExists(trainingId))) {
            throw new TrainingNotFoundException("Training with id " + trainingId + " does not exist");
        }

        var user = userClient.getUser(userId);

        var training = trainingClient.getTrainingById(trainingId);

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

        return enrollmentMapper.toDto(saved);
    }

    @Transactional
    public void withdrawUser(Long userId, Long trainingId) {
        EnrollmentEntity enrollment = enrollmentRepository.findByUserIdAndTrainingId(userId, trainingId)
                .orElseThrow(() -> new UserNotEnrolledException("User is not enrolled for this training"));

        var user = userClient.getUser(userId);
        var training = trainingClient.getTrainingById(trainingId);

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
    }

    public List<EnrollmentDto> getUserEnrollments(Long userId) {
        return enrollmentRepository.findAllByUserId(userId).stream()
                .map(enrollmentMapper::toDto)
                .toList();
    }

    public List<EnrollmentDto> getTrainingEnrollments(Long trainingId) {
        return enrollmentRepository.findAllByTrainingId(trainingId).stream()
                .map(enrollmentMapper::toDto)
                .toList();
    }

    public boolean isUserEnrolledInSession(Long userId, Long trainingId) {
        return enrollmentRepository.findByUserIdAndTrainingId(userId, trainingId).isPresent();
    }
}

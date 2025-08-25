package com.karate.enrollment_service.domain.service;

import com.karate.enrollment_service.domain.exception.TrainingNotFoundException;
import com.karate.enrollment_service.domain.exception.UserAlreadyEnrolledException;
import com.karate.enrollment_service.domain.exception.UserNotEnrolledException;
import com.karate.enrollment_service.domain.exception.UserNotFoundException;
import com.karate.enrollment_service.domain.model.EnrollmentEntity;
import com.karate.enrollment_service.domain.repository.EnrollmentRepository;
import com.karate.enrollment_service.infrastructure.client.TrainingClient;
import com.karate.enrollment_service.infrastructure.client.UserClient;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserClient userClient;
    private final TrainingClient trainingClient;

    @Transactional
    public EnrollmentEntity enrollUser(Long userId, Long trainingId) {
        if (Boolean.FALSE.equals(userClient.checkUserExists(userId))) {
            throw new UserNotFoundException("User with id " + userId + " does not exist");
        }

        if (Boolean.FALSE.equals(trainingClient.checkTrainingSessionExists(trainingId))) {
            throw new TrainingNotFoundException("Training with id " + trainingId + " does not exist");
        }

        enrollmentRepository.findByUserIdAndTrainingId(userId, trainingId)
                .ifPresent(e -> {
                    throw new UserAlreadyEnrolledException("User already enrolled for this training");
                });

        EnrollmentEntity enrollment = EnrollmentEntity.builder()
                .userId(userId)
                .trainingId(trainingId)
                .enrolledAt(LocalDateTime.now())
                .build();

        return enrollmentRepository.save(enrollment);
    }

    @Transactional
    public void withdrawUser(Long userId, Long trainingId) {
        EnrollmentEntity enrollment = enrollmentRepository.findByUserIdAndTrainingId(userId, trainingId)
                .orElseThrow(() -> new UserNotEnrolledException("User is not enrolled for this training"));

        enrollmentRepository.delete(enrollment);
    }

    public List<EnrollmentEntity> getUserEnrollments(Long userId) {
        return enrollmentRepository.findAllByUserId(userId);
    }

    public List<EnrollmentEntity> getTrainingEnrollments(Long trainingId) {
        return enrollmentRepository.findAllByTrainingId(trainingId);
    }
}

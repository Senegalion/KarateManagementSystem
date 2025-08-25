package com.karate.enrollment_service.domain.repository;

import com.karate.enrollment_service.domain.model.EnrollmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<EnrollmentEntity, Long> {
    Optional<EnrollmentEntity> findByUserIdAndTrainingId(Long userId, Long trainingId);

    List<EnrollmentEntity> findAllByUserId(Long userId);

    List<EnrollmentEntity> findAllByTrainingId(Long trainingId);

    void deleteByUserIdAndTrainingId(Long userId, Long trainingId);
}
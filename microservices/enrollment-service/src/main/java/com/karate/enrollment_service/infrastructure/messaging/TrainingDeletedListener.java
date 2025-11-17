package com.karate.enrollment_service.infrastructure.messaging;

import com.karate.enrollment_service.domain.repository.EnrollmentRepository;
import com.karate.training_service.infrastructure.messaging.event.TrainingDeletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrainingDeletedListener {

    private final EnrollmentRepository enrollmentRepository;

    @KafkaListener(
            topics = "${topics.training-deleted}",
            groupId = "enrollment-service-training-deleted"
    )
    @Transactional
    public void onTrainingDeleted(TrainingDeletedEvent evt) {
        Long trainingId = evt.trainingId();
        log.info("enrollment-service: TrainingDeletedEvent trainingId={}", trainingId);

        long deleted = enrollmentRepository.deleteByTrainingId(trainingId);

        log.info("enrollment-service: deleted {} enrollments for trainingId={}", deleted, trainingId);
    }
}

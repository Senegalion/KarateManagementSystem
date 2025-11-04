package com.karate.enrollment_service.infrastructure.messaging;

import com.karate.enrollment_service.domain.repository.EnrollmentRepository;
import com.karate.enrollment_service.infrastructure.messaging.event.UserDeletedEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDeletedListener {

    private final EnrollmentRepository enrollmentRepository;

    @KafkaListener(topics = "${topics.user-deleted}", groupId = "enrollment-service-user-deleted")
    @Transactional
    public void onUserDeleted(UserDeletedEvent evt) {
        Long userId = evt.userId();
        log.info("enrollment-service: UserDeletedEvent userId={}", userId);
        enrollmentRepository.deleteByUserId(userId);
    }
}

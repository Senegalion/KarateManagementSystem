package com.karate.authservice.infrastructure.messaging;

import com.karate.authservice.domain.repository.AuthUserRepository;
import com.karate.authservice.infrastructure.messaging.dto.UserDeletedEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDeletedListener {

    private final AuthUserRepository authUserRepository;


    @KafkaListener(topics = "${topics.user-deleted}", groupId = "auth-service-user-deleted")
    @Transactional
    public void onUserDeleted(UserDeletedEvent evt) {
        Long userId = evt.userId();
        log.info("auth-service: UserDeletedEvent userId={}", userId);
        authUserRepository.deleteByUserId(userId);
    }
}

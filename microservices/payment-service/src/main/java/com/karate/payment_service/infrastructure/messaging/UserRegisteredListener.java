package com.karate.payment_service.infrastructure.messaging;

import com.karate.payment_service.domain.model.UserAccountEntity;
import com.karate.payment_service.domain.repository.UserAccountRepository;
import com.karate.payment_service.infrastructure.messaging.dto.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegisteredListener {

    private final UserAccountRepository repo;

    @Value("${topics.user-registered}")
    private String userTopic;

    @KafkaListener(
            topics = "${topics.user-registered}",
            groupId = "payment-service",
            containerFactory = "userRegisteredListenerFactory"
    )
    public void onUserRegistered(UserRegisteredEvent ev) {
        var p = ev.getPayload();
        var existing = repo.findById(p.getUserId()).orElse(null);
        if (existing == null) {
            var ua = UserAccountEntity.builder()
                    .userId(p.getUserId())
                    .email(p.getUserEmail())
                    .username(p.getUsername())
                    .registrationDate(p.getRegistrationDate())
                    .clubId(p.getClubId())
                    .clubName(p.getClubName())
                    .karateRank(p.getKarateRank())
                    .build();
            repo.save(ua);
            log.info("UserAccount snapshot created userId={}", p.getUserId());
        } else {
            existing.setEmail(p.getUserEmail());
            existing.setUsername(p.getUsername());
            existing.setClubId(p.getClubId());
            existing.setClubName(p.getClubName());
            existing.setKarateRank(p.getKarateRank());
            repo.save(existing);
            log.info("UserAccount snapshot updated userId={}", p.getUserId());
        }
    }
}

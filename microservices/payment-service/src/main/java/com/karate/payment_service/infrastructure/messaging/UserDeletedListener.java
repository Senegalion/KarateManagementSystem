package com.karate.payment_service.infrastructure.messaging;

import com.karate.payment_service.domain.repository.PaymentItemRepository;
import com.karate.payment_service.domain.repository.PaymentRepository;
import com.karate.payment_service.domain.repository.UserAccountRepository;
import com.karate.payment_service.infrastructure.messaging.dto.UserDeletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDeletedListener {

    private final PaymentRepository paymentRepository;
    private final UserAccountRepository userAccountRepository;
    private final PaymentItemRepository paymentItemRepository;

    @KafkaListener(
            topics = "${topics.user-deleted}",
            groupId = "payment-service-user-deleted",
            properties = {
                    "spring.json.use.type.headers=false",
                    "spring.json.value.default.type=com.karate.payment_service.infrastructure.messaging.dto.UserDeletedEvent",
                    "spring.deserializer.value.delegate.class=org.springframework.kafka.support.serializer.JsonDeserializer"
            }
    )
    @Transactional
    public void onUserDeleted(UserDeletedEvent evt) {
        Long userId = evt.userId();
        log.info("payment-service: UserDeletedEvent userId={}", userId);

        int deletedPaymentItems = paymentItemRepository.deleteByUserId(userId);
        int deletedPayments = paymentRepository.deleteAllByUserId(userId);
        int deletedAccounts = userAccountRepository.deleteByUserId(userId);

        log.info(
                "payment-service: deleted payments={}, user_account={}, items={} for userId={}",
                deletedPayments, deletedAccounts, deletedPaymentItems, userId
        );
    }
}

package com.karate.payment_service.infrastructure.messaging;

import com.karate.payment_service.domain.repository.PaymentItemRepository;
import com.karate.payment_service.domain.repository.PaymentRepository;
import com.karate.payment_service.domain.repository.UserAccountRepository;
import com.karate.payment_service.infrastructure.messaging.dto.UserDeletedEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.mockito.Mockito.*;

class UserDeletedListenerTest {

    @Test
    void onUserDeleted_deletesFromAllRepositories() {
        PaymentRepository p = mock(PaymentRepository.class);
        UserAccountRepository u = mock(UserAccountRepository.class);
        PaymentItemRepository i = mock(PaymentItemRepository.class);

        when(i.deleteByUserId(7L)).thenReturn(3);
        when(p.deleteAllByUserId(7L)).thenReturn(2);
        when(u.deleteByUserId(7L)).thenReturn(1);

        UserDeletedListener l = new UserDeletedListener(p, u, i);

        l.onUserDeleted(new UserDeletedEvent("e", "USER_DELETED", Instant.now(), 7L));

        verify(i).deleteByUserId(7L);
        verify(p).deleteAllByUserId(7L);
        verify(u).deleteByUserId(7L);
    }
}

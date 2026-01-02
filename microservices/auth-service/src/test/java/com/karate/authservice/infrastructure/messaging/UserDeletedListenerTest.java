package com.karate.authservice.infrastructure.messaging;

import com.karate.authservice.domain.repository.AuthUserRepository;
import com.karate.authservice.infrastructure.messaging.dto.UserDeletedEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class UserDeletedListenerTest {

    @Test
    void onUserDeleted_deletesByUserId() {
        // given
        AuthUserRepository repo = mock(AuthUserRepository.class);
        UserDeletedListener listener = new UserDeletedListener(repo);

        UserDeletedEvent event = new UserDeletedEvent(
                "evt-1",
                "USER_DELETED",
                Instant.now(),
                10L
        );

        // when
        listener.onUserDeleted(event);

        // then
        verify(repo).deleteByUserId(10L);
    }
}
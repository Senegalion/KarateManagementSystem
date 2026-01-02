package com.karate.payment_service.infrastructure.messaging;

import com.karate.payment_service.domain.model.UserAccountEntity;
import com.karate.payment_service.domain.repository.UserAccountRepository;
import com.karate.payment_service.infrastructure.messaging.dto.UserRegisteredEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.Mockito.*;

class UserRegisteredListenerTest {

    @Test
    void onUserRegistered_createsSnapshot_whenMissing() {
        UserAccountRepository repo = mock(UserAccountRepository.class);
        UserRegisteredListener l = new UserRegisteredListener(repo);

        UserRegisteredEvent.Payload p = new UserRegisteredEvent.Payload(
                1L, "a@b.com", "john", 10L, "Club", "WHITE", LocalDate.of(2025, 1, 1)
        );
        UserRegisteredEvent ev = new UserRegisteredEvent("e1", "USER_REGISTERED", Instant.now(), p);

        when(repo.findById(1L)).thenReturn(Optional.empty());

        l.onUserRegistered(ev);

        verify(repo).save(argThat(ua ->
                ua.getUserId().equals(1L) &&
                        ua.getEmail().equals("a@b.com") &&
                        ua.getUsername().equals("john") &&
                        ua.getClubId().equals(10L)
        ));
    }

    @Test
    void onUserRegistered_updatesSnapshot_whenExists() {
        UserAccountRepository repo = mock(UserAccountRepository.class);
        UserRegisteredListener l = new UserRegisteredListener(repo);

        UserAccountEntity existing = UserAccountEntity.builder()
                .userId(1L).email("old").username("old").registrationDate(LocalDate.of(2025, 1, 1))
                .clubId(1L).clubName("x").karateRank("y")
                .build();

        when(repo.findById(1L)).thenReturn(Optional.of(existing));

        UserRegisteredEvent.Payload p = new UserRegisteredEvent.Payload(
                1L, "new@b.com", "newname", 99L, "NewClub", "BROWN", LocalDate.of(2025, 1, 1)
        );
        UserRegisteredEvent ev = new UserRegisteredEvent("e2", "USER_REGISTERED", Instant.now(), p);

        l.onUserRegistered(ev);

        verify(repo).save(existing);
    }
}

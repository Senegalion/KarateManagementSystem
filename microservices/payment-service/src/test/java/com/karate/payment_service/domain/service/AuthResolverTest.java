package com.karate.payment_service.domain.service;

import com.karate.payment_service.infrastructure.client.AuthClient;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthResolverTest {

    @Test
    void resolveUserId_throws_whenAuthNull() {
        AuthClient authClient = mock(AuthClient.class);
        AuthResolver r = new AuthResolver(authClient);

        assertThatThrownBy(() -> r.resolveUserId(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not authenticated");
    }

    @Test
    void resolveUserId_throws_whenNotAuthenticated() {
        AuthClient authClient = mock(AuthClient.class);
        AuthResolver r = new AuthResolver(authClient);

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);

        assertThatThrownBy(() -> r.resolveUserId(auth))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void resolveUserId_throws_whenClientReturnsNull() {
        AuthClient authClient = mock(AuthClient.class);
        AuthResolver r = new AuthResolver(authClient);

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("john");
        when(authClient.getUserIdByUsername("john")).thenReturn(null);

        assertThatThrownBy(() -> r.resolveUserId(auth))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("UserId not found")
                .hasMessageContaining("john");
    }

    @Test
    void resolveUserId_returnsId() {
        AuthClient authClient = mock(AuthClient.class);
        AuthResolver r = new AuthResolver(authClient);

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("john");
        when(authClient.getUserIdByUsername("john")).thenReturn(42L);

        assertThat(r.resolveUserId(auth)).isEqualTo(42L);
    }
}

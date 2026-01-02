package com.karate.userservice.unit.service;

import com.karate.userservice.domain.exception.UpstreamUnavailableException;
import com.karate.userservice.domain.service.UpstreamGateway;
import com.karate.userservice.infrastructure.client.AuthClient;
import com.karate.userservice.infrastructure.client.KarateClubClient;
import com.karate.userservice.infrastructure.client.dto.AuthUserDto;
import com.karate.userservice.infrastructure.client.dto.KarateClubDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class UpstreamGatewayTest {

    @Mock
    private AuthClient authClient;
    @Mock
    private KarateClubClient clubClient;

    @InjectMocks
    private UpstreamGateway gateway;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("getAuthUserByUserId delegates to authClient")
    void get_auth_user_by_user_id_delegates() {
        // given
        when(authClient.getAuthUserByUserId(10L))
                .thenReturn(new AuthUserDto(10L, "john", Set.of("ROLE_USER")));

        // when
        gateway.getAuthUserByUserId(10L);

        // then
        verify(authClient).getAuthUserByUserId(10L);
    }

    @Test
    @DisplayName("getAuthUserByUsername delegates to authClient")
    void get_auth_user_by_username_delegates() {
        // given
        when(authClient.getAuthUserByUsername("john"))
                .thenReturn(new AuthUserDto(10L, "john", Set.of("ROLE_USER")));

        // when
        gateway.getAuthUserByUsername("john");

        // then
        verify(authClient).getAuthUserByUsername("john");
    }

    @Test
    @DisplayName("getAuthUsers delegates list to authClient")
    void get_auth_users_delegates_list() {
        // given
        var ids = List.of(1L, 2L);
        when(authClient.getAuthUsers(ids)).thenReturn(Map.of(
                1L, new AuthUserDto(1L, "a", Set.of()),
                2L, new AuthUserDto(2L, "b", Set.of())
        ));

        // when
        gateway.getAuthUsers(ids);

        // then
        verify(authClient).getAuthUsers(ids);
    }

    @Test
    @DisplayName("updateUsername returns completed future and calls authClient")
    void update_username_returns_future_and_calls_client() {
        // given
        doNothing().when(authClient).updateUsername(5L, "x");

        // when
        gateway.updateUsername(5L, "x").join();

        // then
        verify(authClient).updateUsername(5L, "x");
    }

    @Test
    @DisplayName("deleteUser returns completed future and calls authClient")
    void delete_user_returns_future_and_calls_client() {
        // given
        doNothing().when(authClient).deleteUser(7L);

        // when
        gateway.deleteUser(7L).join();

        // then
        verify(authClient).deleteUser(7L);
    }

    @Test
    @DisplayName("getClubByName delegates to clubClient")
    void get_club_by_name_delegates() {
        // given
        when(clubClient.getClubByName("TOKYO")).thenReturn(new KarateClubDto(9L, "TOKYO"));

        // when
        gateway.getClubByName("TOKYO");

        // then
        verify(clubClient).getClubByName("TOKYO");
    }

    @Test
    @DisplayName("getClubById delegates to clubClient")
    void get_club_by_id_delegates() {
        // given
        when(clubClient.getClubById(9L)).thenReturn(new KarateClubDto(9L, "TOKYO"));

        // when
        gateway.getClubById(9L);

        // then
        verify(clubClient).getClubById(9L);
    }

    @Test
    @DisplayName("fallback getAuthUserByUserId throws UpstreamUnavailableException")
    void fallback_get_auth_user_by_user_id_throws() {
        var ex = new RuntimeException("down");
        assertThatThrownBy(() ->
                gateway.getAuthUserByUserIdFallback(1L, ex)
        ).isInstanceOf(com.karate.userservice.domain.exception.UpstreamUnavailableException.class)
                .hasMessageContaining("auth-service unavailable")
                .hasCause(ex);
    }

    @Test
    @DisplayName("fallback getAuthUserByUsername throws UpstreamUnavailableException")
    void fallback_get_auth_user_by_username_throws() {
        var ex = new RuntimeException("down");

        assertThatThrownBy(() -> gateway.getAuthUserByUsernameFallback("john", ex))
                .isInstanceOf(UpstreamUnavailableException.class)
                .hasMessageContaining("auth-service unavailable")
                .hasCause(ex);
    }

    @Test
    @DisplayName("fallback getAuthUsers throws UpstreamUnavailableException (ids is Collection)")
    void fallback_get_auth_users_throws_for_collection_ids() {
        var ex = new RuntimeException("down");
        var ids = List.of(1L, 2L, 3L);

        assertThatThrownBy(() -> gateway.getAuthUsersFallback(ids, ex))
                .isInstanceOf(UpstreamUnavailableException.class)
                .hasMessageContaining("auth-service unavailable")
                .hasCause(ex);
    }

    @Test
    @DisplayName("fallback getAuthUsers throws UpstreamUnavailableException (ids is not Collection)")
    void fallback_get_auth_users_throws_for_non_collection_ids() {
        var ex = new RuntimeException("down");

        Iterable<Long> ids = () -> List.of(1L, 2L).iterator();

        assertThatThrownBy(() -> gateway.getAuthUsersFallback(ids, ex))
                .isInstanceOf(UpstreamUnavailableException.class)
                .hasMessageContaining("auth-service unavailable")
                .hasCause(ex);
    }

    @Test
    @DisplayName("fallback updateUsername returns failed future with UpstreamUnavailableException")
    void fallback_update_username_returns_failed_future() {
        var cause = new RuntimeException("timeout");

        var fut = gateway.updateUsernameFallback(5L, "newName", cause);

        assertThatThrownBy(fut::join)
                .isInstanceOf(CompletionException.class)
                .hasCauseInstanceOf(UpstreamUnavailableException.class)
                .hasMessageContaining("auth-service timeout")
                .hasRootCause(cause)
                .hasRootCauseMessage("timeout");
    }

    @Test
    @DisplayName("fallback deleteUser returns failed future with UpstreamUnavailableException")
    void fallback_delete_user_returns_failed_future() {
        var cause = new RuntimeException("timeout");

        var fut = gateway.deleteUserFallback(7L, cause);

        assertThatThrownBy(fut::join)
                .isInstanceOf(CompletionException.class)
                .hasCauseInstanceOf(UpstreamUnavailableException.class)
                .hasMessageContaining("auth-service timeout")
                .hasRootCause(cause)
                .hasRootCauseMessage("timeout");
    }

    @Test
    @DisplayName("fallback getClubByName throws UpstreamUnavailableException")
    void fallback_get_club_by_name_throws() {
        var ex = new RuntimeException("down");

        assertThatThrownBy(() -> gateway.getClubByNameFallback("TOKYO", ex))
                .isInstanceOf(UpstreamUnavailableException.class)
                .hasMessageContaining("club-service unavailable")
                .hasCause(ex);
    }

    @Test
    @DisplayName("fallback getClubById throws UpstreamUnavailableException")
    void fallback_get_club_by_id_throws() {
        var ex = new RuntimeException("down");

        assertThatThrownBy(() -> gateway.getClubByIdFallback(9L, ex))
                .isInstanceOf(UpstreamUnavailableException.class)
                .hasMessageContaining("club-service unavailable")
                .hasCause(ex);
    }

}

package com.karate.authservice.unit.service;

import com.karate.authservice.domain.service.UpstreamGateway;
import com.karate.authservice.infrastructure.client.KarateClubClient;
import com.karate.authservice.infrastructure.client.UserClient;
import com.karate.authservice.infrastructure.client.dto.AddressDto;
import com.karate.authservice.infrastructure.client.dto.KarateClubDto;
import com.karate.authservice.infrastructure.client.dto.NewUserRequestDto;
import com.karate.authservice.infrastructure.client.dto.UserInfoDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpstreamGatewayTest {

    @Mock
    UserClient userClient;
    @Mock
    KarateClubClient clubClient;

    @Test
    void getUserById_success_returnsUserInfo() {
        // given
        when(userClient.getUserById(10L)).thenReturn(new UserInfoDto(10L, "a@b", 21L, "KYU_9"));

        // when
        var gw = new UpstreamGateway(userClient, clubClient);
        var info = gw.getUserById(10L);

        // then
        assertThat(info.email()).isEqualTo("a@b");
        assertThat(info.karateClubId()).isEqualTo(21L);
        assertThat(info.karateRank()).isEqualTo("KYU_9");
    }

    @Test
    void getUserById_whenClientThrows_propagatesOriginalExceptionInUnitTest() {
        // given
        when(userClient.getUserById(10L)).thenThrow(new RuntimeException("boom"));

        // when
        var gw = new UpstreamGateway(userClient, clubClient);

        // then
        assertThatThrownBy(() -> gw.getUserById(10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("boom");
    }

    @Test
    void getClubById_success_returnsClub() {
        // given
        when(clubClient.getClubById(21L)).thenReturn(new KarateClubDto(21L, "TOKYO"));

        // when
        var gw = new UpstreamGateway(userClient, clubClient);
        var club = gw.getClubById(21L);

        // then
        assertThat(club.karateClubId()).isEqualTo(21L);
        assertThat(club.name()).isEqualTo("TOKYO");
    }

    @Test
    void getClubById_whenClientThrows_propagatesOriginalExceptionInUnitTest() {
        // given
        when(clubClient.getClubById(21L)).thenThrow(new RuntimeException("nope"));

        // when
        var gw = new UpstreamGateway(userClient, clubClient);

        // then
        assertThatThrownBy(() -> gw.getClubById(21L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("nope");
    }

    @Test
    void getClubByName_success_returnsClub() {
        // given
        when(clubClient.getClubByName("TOKYO")).thenReturn(new KarateClubDto(21L, "TOKYO"));

        // when
        var gw = new UpstreamGateway(userClient, clubClient);
        var club = gw.getClubByName("TOKYO");

        // then
        assertThat(club.karateClubId()).isEqualTo(21L);
        assertThat(club.name()).isEqualTo("TOKYO");
    }

    @Test
    void getClubByName_whenClientThrows_propagatesOriginalExceptionInUnitTest() {
        // given
        when(clubClient.getClubByName("TOKYO")).thenThrow(new RuntimeException("down"));

        // when
        var gw = new UpstreamGateway(userClient, clubClient);

        // then
        assertThatThrownBy(() -> gw.getClubByName("TOKYO"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("down");
    }

    @Test
    void createUserAsync_success_returnsId() {
        // given
        var dto = new NewUserRequestDto(
                1L, "a@b", 21L, "KYU_9",
                new AddressDto("C", "S", "1", "00-000")
        );
        when(userClient.createUser(any())).thenReturn(777L);

        // when
        var gw = new UpstreamGateway(userClient, clubClient);
        Long id = gw.createUserAsync(dto).join();

        // then
        assertThat(id).isEqualTo(777L);
    }

    @Test
    void createUserAsync_whenClientThrows_futureCompletesExceptionally() {
        // given
        var dto = new NewUserRequestDto(
                1L, "a@b", 21L, "KYU_9",
                new AddressDto("C", "S", "1", "00-000")
        );
        when(userClient.createUser(any())).thenThrow(new IllegalStateException("create-failed"));

        // when
        var gw = new UpstreamGateway(userClient, clubClient);

        // then
        assertThatThrownBy(() -> gw.createUserAsync(dto).join())
                .isInstanceOf(CompletionException.class)
                .hasCauseInstanceOf(IllegalStateException.class)
                .hasRootCauseMessage("create-failed");
    }
}

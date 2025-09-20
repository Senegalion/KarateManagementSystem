package com.karate.userservice.unit.service;

import com.karate.userservice.api.dto.AddressRequestDto;
import com.karate.userservice.api.dto.NewUserRequestDto;
import com.karate.userservice.api.dto.UpdateUserRequestDto;
import com.karate.userservice.api.dto.UserFromClubDto;
import com.karate.userservice.domain.exception.UserNotFoundException;
import com.karate.userservice.domain.model.AddressEntity;
import com.karate.userservice.domain.model.KarateRank;
import com.karate.userservice.domain.model.UserEntity;
import com.karate.userservice.domain.model.dto.AddressDto;
import com.karate.userservice.domain.repository.UserRepository;
import com.karate.userservice.domain.service.UpstreamGateway;
import com.karate.userservice.domain.service.UserService;
import com.karate.userservice.infrastructure.client.dto.AuthUserDto;
import com.karate.userservice.infrastructure.client.dto.KarateClubDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UpstreamGateway upstream;
    @InjectMocks
    private UserService service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    private static UserEntity user(Long id, String email, Long clubId, KarateRank rank) {
        var addr = AddressEntity.builder()
                .city("C").street("S").number("1").postalCode("00-000").build();
        var u = UserEntity.builder()
                .userId(id)
                .email(email)
                .karateClubId(clubId)
                .karateRank(rank)
                .registrationDate(LocalDate.now())
                .addressEntity(addr)
                .build();
        addr.setUserEntity(u);
        return u;
    }

    // ---------------- getUsersFromClubByName ----------------
    @Test
    @DisplayName("getUsersFromClubByName returns users with usernames and roles from auth-service")
    void get_users_from_club_by_name_returns_users() {
        // given
        when(upstream.getClubByName("TOKYO")).thenReturn(new KarateClubDto(5L, "TOKYO"));
        var u1 = user(10L, "a@b", 5L, KarateRank.KYU_10);
        var u2 = user(11L, "c@d", 5L, KarateRank.KYU_9);
        when(userRepository.findAllByKarateClubId(5L)).thenReturn(List.of(u1, u2));
        when(upstream.getAuthUserByUserId(10L)).thenReturn(new AuthUserDto(10L, "john", Set.of("ROLE_USER")));
        when(upstream.getAuthUserByUserId(11L)).thenReturn(new AuthUserDto(11L, "mary", Set.of("ROLE_ADMIN")));

        // when
        var out = service.getUsersFromClubByName("TOKYO");

        // then
        assertThat(out).hasSize(2);
        assertThat(out).extracting(UserFromClubDto::userId).containsExactlyInAnyOrder(10L, 11L);
    }

    @Test
    @DisplayName("getUsersFromClubByName returns empty list if no users in repository")
    void get_users_from_club_by_name_returns_empty_list_when_no_users() {
        // given
        when(upstream.getClubByName("EMPTY")).thenReturn(new KarateClubDto(7L, "EMPTY"));
        when(userRepository.findAllByKarateClubId(7L)).thenReturn(List.of());

        // when
        var out = service.getUsersFromClubByName("EMPTY");

        // then
        assertThat(out).isEmpty();
        verify(upstream, never()).getAuthUserByUserId(anyLong());
    }

    // ---------------- createUser ----------------
    @Test
    @DisplayName("createUser saves entity with address and returns ID")
    void create_user_saves_entity_and_returns_id() {
        // given
        var req = new NewUserRequestDto(
                55L, "mail@ex.com", 3L, "KYU_8",
                new AddressDto("City", "Street", "12", "00-111")
        );
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // when
        Long id = service.createUser(req);

        // then
        assertThat(id).isEqualTo(55L);
        verify(userRepository).save(any());
    }

    // ---------------- getUserById ----------------
    @Test
    @DisplayName("getUserById returns dto when user exists")
    void get_user_by_id_returns_dto_when_exists() {
        // given
        when(userRepository.findById(10L)).thenReturn(Optional.of(
                user(10L, "a@b", 5L, KarateRank.KYU_10)
        ));

        // when
        var dto = service.getUserById(10L);

        // then
        assertThat(dto.userId()).isEqualTo(10L);
        assertThat(dto.karateClubId()).isEqualTo(5L);
        assertThat(dto.karateRank()).isEqualTo("KYU_10");
        assertThat(dto.email()).isEqualTo("a@b");
    }

    @Test
    @DisplayName("getUserById throws UserNotFoundException when user not found")
    void get_user_by_id_throws_exception_when_missing() {
        // given
        when(userRepository.findById(111L)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> service.getUserById(111L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(UserService.USER_NOT_FOUND);
    }

    // ---------------- getCurrentUserInfo ----------------
    @Test
    @DisplayName("getCurrentUserInfo returns merged data from auth and club service")
    void get_current_user_info_returns_merged_data() {
        // given
        when(upstream.getAuthUserByUsername("john"))
                .thenReturn(new AuthUserDto(77L, "john", Set.of("ROLE_USER")));
        when(userRepository.findById(77L))
                .thenReturn(Optional.of(user(77L, "john@ex.com", 9L, KarateRank.KYU_9)));
        when(upstream.getClubById(9L))
                .thenReturn(new KarateClubDto(9L, "TOKYO"));

        // when
        var out = service.getCurrentUserInfo("john");

        // then
        assertThat(out.userId()).isEqualTo(77L);
        assertThat(out.username()).isEqualTo("john");
        assertThat(out.email()).isEqualTo("john@ex.com");
        assertThat(out.karateClubName()).isEqualTo("TOKYO");
        assertThat(out.karateRank()).isEqualTo("KYU_9");
        assertThat(out.roles()).containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("getCurrentUserInfo throws UserNotFoundException when user missing in DB")
    void get_current_user_info_throws_exception_when_missing_in_db() {
        // given
        when(upstream.getAuthUserByUsername("ghost"))
                .thenReturn(new AuthUserDto(999L, "ghost", Set.of("ROLE_USER")));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> service.getCurrentUserInfo("ghost"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(UserService.USER_NOT_FOUND);
        verify(upstream, never()).getClubById(anyLong());
    }

    // ---------------- getCurrentUserClubIdByUsername ----------------
    @Test
    @DisplayName("getCurrentUserClubIdByUsername returns clubId")
    void get_current_user_club_id_by_username_returns_club_id() {
        // given
        when(upstream.getAuthUserByUsername("mary"))
                .thenReturn(new AuthUserDto(88L, "mary", Set.of("ROLE_USER")));
        when(userRepository.findById(88L))
                .thenReturn(Optional.of(user(88L, "m@ex", 42L, KarateRank.KYU_8)));

        // when
        Long clubId = service.getCurrentUserClubIdByUsername("mary");

        // then
        assertThat(clubId).isEqualTo(42L);
    }

    @Test
    @DisplayName("getCurrentUserClubIdByUsername throws exception when missing")
    void get_current_user_club_id_by_username_throws_exception_when_missing() {
        // given
        when(upstream.getAuthUserByUsername("x"))
                .thenReturn(new AuthUserDto(1L, "x", Set.of("ROLE_USER")));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> service.getCurrentUserClubIdByUsername("x"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(UserService.USER_NOT_FOUND);
    }

    // ---------------- checkUserExists ----------------
    @Test
    @DisplayName("checkUserExists returns true when user exists")
    void check_user_exists_returns_true() {
        // given
        when(userRepository.existsById(3L)).thenReturn(true);

        // when / then
        assertThat(service.checkUserExists(3L)).isTrue();
    }

    @Test
    @DisplayName("checkUserExists returns false when user does not exist")
    void check_user_exists_returns_false() {
        // given
        when(userRepository.existsById(4L)).thenReturn(false);

        // when / then
        assertThat(service.checkUserExists(4L)).isFalse();
    }

    // ---------------- getUser ----------------
    @Test
    @DisplayName("getUser returns payload with username from auth service")
    void get_user_returns_payload_with_username() {
        // given
        when(userRepository.findById(10L))
                .thenReturn(Optional.of(user(10L, "a@b", 5L, KarateRank.KYU_10)));
        when(upstream.getAuthUserByUserId(10L))
                .thenReturn(new AuthUserDto(10L, "john", Set.of("ROLE_USER")));

        // when
        var payload = service.getUser(10L);

        // then
        assertThat(payload.userId()).isEqualTo(10L);
        assertThat(payload.userEmail()).isEqualTo("a@b");
        assertThat(payload.username()).isEqualTo("john");
    }

    @Test
    @DisplayName("getUser throws exception when user not found")
    void get_user_throws_exception_when_not_found() {
        // given
        when(userRepository.findById(9L)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> service.getUser(9L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(UserService.USER_NOT_FOUND);
        verify(upstream, never()).getAuthUserByUserId(anyLong());
    }

    // ---------------- updateCurrentUser ----------------
    @Test
    @DisplayName("updateCurrentUser updates username, email and address")
    void update_current_user_updates_all_fields() {
        // given
        when(upstream.getAuthUserByUsername("john"))
                .thenReturn(new AuthUserDto(77L, "john", Set.of("ROLE_USER")));
        var ent = user(77L, "old@ex", 1L, KarateRank.KYU_9);
        when(userRepository.findById(77L)).thenReturn(Optional.of(ent));
        // IMPORTANT: stub async call because production code calls .join()
        when(upstream.updateUsername(77L, "newjohn"))
                .thenReturn(CompletableFuture.completedFuture(null));

        var req = new UpdateUserRequestDto(
                "newjohn", "new@ex",
                new AddressRequestDto("C", "D", "2", "11-111")
        );

        // when
        service.updateCurrentUser("john", req);

        // then
        verify(upstream).updateUsername(77L, "newjohn");
        verify(userRepository).save(ent);
        assertThat(ent.getEmail()).isEqualTo("new@ex");
        assertThat(ent.getAddressEntity().getCity()).isEqualTo("C");
        assertThat(ent.getAddressEntity().getStreet()).isEqualTo("D");
        assertThat(ent.getAddressEntity().getNumber()).isEqualTo("2");
        assertThat(ent.getAddressEntity().getPostalCode()).isEqualTo("11-111");
    }

    @Test
    @DisplayName("updateCurrentUser throws exception when user missing")
    void update_current_user_throws_exception_when_missing() {
        // given
        when(upstream.getAuthUserByUsername("john"))
                .thenReturn(new AuthUserDto(77L, "john", Set.of("ROLE_USER")));
        when(userRepository.findById(77L)).thenReturn(Optional.empty());
        var req = new UpdateUserRequestDto("x", "y@z", new AddressRequestDto("C", "S", "1", "00-000"));

        // when / then
        assertThatThrownBy(() -> service.updateCurrentUser("john", req))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(UserService.USER_NOT_FOUND);
        verify(upstream, never()).updateUsername(anyLong(), anyString());
        verify(userRepository, never()).save(any());
    }

    // ---------------- patchCurrentUser ----------------
    @Test
    @DisplayName("patchCurrentUser updates only username when others are null")
    void patch_current_user_updates_only_username() {
        // given
        when(upstream.getAuthUserByUsername("u"))
                .thenReturn(new AuthUserDto(1L, "u", Set.of("ROLE_USER")));
        var ent = user(1L, "old@ex", 1L, KarateRank.KYU_10);
        when(userRepository.findById(1L)).thenReturn(Optional.of(ent));
        when(upstream.updateUsername(1L, "newU"))
                .thenReturn(CompletableFuture.completedFuture(null));

        var req = new UpdateUserRequestDto("newU", null, null);

        // when
        service.patchCurrentUser("u", req);

        // then
        verify(upstream).updateUsername(1L, "newU");
        verify(userRepository).save(ent);
        assertThat(ent.getEmail()).isEqualTo("old@ex");
    }

    @Test
    @DisplayName("patchCurrentUser updates only email when username and address are null")
    void patch_current_user_updates_only_email() {
        // given
        when(upstream.getAuthUserByUsername("u"))
                .thenReturn(new AuthUserDto(1L, "u", Set.of("ROLE_USER")));
        var ent = user(1L, "old@ex", 1L, KarateRank.KYU_10);
        when(userRepository.findById(1L)).thenReturn(Optional.of(ent));

        var req = new UpdateUserRequestDto(null, "new@ex", null);

        // when
        service.patchCurrentUser("u", req);

        // then
        assertThat(ent.getEmail()).isEqualTo("new@ex");
        verify(userRepository).save(ent);
    }

    @Test
    @DisplayName("patchCurrentUser updates partial address")
    void patch_current_user_updates_partial_address() {
        // given
        when(upstream.getAuthUserByUsername("u"))
                .thenReturn(new AuthUserDto(1L, "u", Set.of("ROLE_USER")));
        var ent = user(1L, "old@ex", 1L, KarateRank.KYU_10);
        ent.getAddressEntity().setCity("A");
        ent.getAddressEntity().setStreet("B");
        ent.getAddressEntity().setNumber("1");
        ent.getAddressEntity().setPostalCode("00-000");
        when(userRepository.findById(1L)).thenReturn(Optional.of(ent));

        var req = new UpdateUserRequestDto(null, null,
                new AddressRequestDto(null, "BB", null, "11-111"));

        // when
        service.patchCurrentUser("u", req);

        // then
        assertThat(ent.getAddressEntity().getCity()).isEqualTo("A");
        assertThat(ent.getAddressEntity().getStreet()).isEqualTo("BB");
        assertThat(ent.getAddressEntity().getNumber()).isEqualTo("1");
        assertThat(ent.getAddressEntity().getPostalCode()).isEqualTo("11-111");
        verify(userRepository).save(ent);
    }

    // ---------------- deleteCurrentUser ----------------
    @Test
    @DisplayName("deleteCurrentUser deletes user from DB and calls upstream")
    void delete_current_user_deletes_user_and_calls_upstream() {
        // given
        when(upstream.getAuthUserByUsername("john"))
                .thenReturn(new AuthUserDto(77L, "john", Set.of("ROLE_USER")));
        var ent = user(77L, "x@x", 1L, KarateRank.KYU_10);
        when(userRepository.findById(77L)).thenReturn(Optional.of(ent));

        // when
        service.deleteCurrentUser("john");

        // then
        verify(userRepository).delete(ent);
        verify(upstream).deleteUser(77L);
    }

    @Test
    @DisplayName("deleteCurrentUser throws exception when user not found")
    void delete_current_user_throws_exception_when_missing() {
        // given
        when(upstream.getAuthUserByUsername("john"))
                .thenReturn(new AuthUserDto(77L, "john", Set.of("ROLE_USER")));
        when(userRepository.findById(77L)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> service.deleteCurrentUser("john"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(UserService.USER_NOT_FOUND);
        verify(userRepository, never()).delete(any());
        verify(upstream, never()).deleteUser(anyLong());
    }

    // --- extra assertions for getUsersFromClubByName mapping ---
    @Test
    @DisplayName("getUsersFromClubByName maps username/roles/rank/email correctly")
    void get_users_from_club_by_name_maps_all_fields() {
        // given
        when(upstream.getClubByName("TOKYO")).thenReturn(new KarateClubDto(5L, "TOKYO"));
        var u1 = user(10L, "a@b", 5L, KarateRank.KYU_10);
        var u2 = user(11L, "c@d", 5L, KarateRank.KYU_9);
        when(userRepository.findAllByKarateClubId(5L)).thenReturn(List.of(u1, u2));
        when(upstream.getAuthUserByUserId(10L)).thenReturn(new AuthUserDto(10L, "john", Set.of("ROLE_USER")));
        when(upstream.getAuthUserByUserId(11L)).thenReturn(new AuthUserDto(11L, "mary", Set.of("ROLE_ADMIN")));

        // when
        var out = service.getUsersFromClubByName("TOKYO");

        // then
        assertThat(out).hasSize(2);
        var john = out.stream().filter(d -> d.userId().equals(10L)).findFirst().orElseThrow();
        assertThat(john.username()).isEqualTo("john");
        assertThat(john.email()).isEqualTo("a@b");
        assertThat(john.roles()).containsExactly("ROLE_USER");
        assertThat(john.karateRank()).isEqualTo(KarateRank.KYU_10.toString());

        var mary = out.stream().filter(d -> d.userId().equals(11L)).findFirst().orElseThrow();
        assertThat(mary.username()).isEqualTo("mary");
        assertThat(mary.email()).isEqualTo("c@d");
        assertThat(mary.roles()).containsExactly("ROLE_ADMIN");
        assertThat(mary.karateRank()).isEqualTo(KarateRank.KYU_9.toString());
    }

    // --- createUser invalid rank ---
    @Test
    @DisplayName("createUser throws IllegalArgumentException when karateRank is invalid")
    void create_user_throws_on_invalid_rank() {
        // given
        var req = new NewUserRequestDto(
                1L, "x@y", 2L, "NOT_A_RANK",
                new AddressDto("City", "Street", "1", "00-000")
        );

        // when / then
        assertThatThrownBy(() -> service.createUser(req))
                .isInstanceOf(IllegalArgumentException.class);
        verify(userRepository, never()).save(any());
    }

    // --- patchCurrentUser full address update (all fields) ---
    @Test
    @DisplayName("patchCurrentUser updates full address when all fields are present")
    void patch_current_user_updates_full_address() {
        // given
        when(upstream.getAuthUserByUsername("u"))
                .thenReturn(new AuthUserDto(1L, "u", Set.of("ROLE_USER")));
        var ent = user(1L, "old@ex", 1L, KarateRank.KYU_10);
        ent.getAddressEntity().setCity("A");
        ent.getAddressEntity().setStreet("B");
        ent.getAddressEntity().setNumber("1");
        ent.getAddressEntity().setPostalCode("00-000");
        when(userRepository.findById(1L)).thenReturn(Optional.of(ent));

        var req = new UpdateUserRequestDto(
                null, null,
                new AddressRequestDto("C", "D", "2", "11-111")
        );

        // when
        service.patchCurrentUser("u", req);

        // then
        assertThat(ent.getAddressEntity().getCity()).isEqualTo("C");
        assertThat(ent.getAddressEntity().getStreet()).isEqualTo("D");
        assertThat(ent.getAddressEntity().getNumber()).isEqualTo("2");
        assertThat(ent.getAddressEntity().getPostalCode()).isEqualTo("11-111");
        verify(userRepository).save(ent);
    }

    // --- updateCurrentUser: ensure async stub prevents NPE (.join) ---
    @Test
    @DisplayName("updateCurrentUser stubs async gateway (join) and persists changes")
    void update_current_user_async_join_stubbed() {
        // given
        when(upstream.getAuthUserByUsername("john"))
                .thenReturn(new AuthUserDto(77L, "john", Set.of("ROLE_USER")));
        var ent = user(77L, "old@ex", 1L, KarateRank.KYU_9);
        when(userRepository.findById(77L)).thenReturn(Optional.of(ent));
        when(upstream.updateUsername(77L, "newjohn"))
                .thenReturn(CompletableFuture.completedFuture(null));
        var req = new UpdateUserRequestDto(
                "newjohn", "new@ex", new AddressRequestDto("C", "D", "2", "11-111")
        );

        // when
        service.updateCurrentUser("john", req);

        // then
        verify(upstream).updateUsername(77L, "newjohn");
        verify(userRepository).save(ent);
        assertThat(ent.getEmail()).isEqualTo("new@ex");
    }

}

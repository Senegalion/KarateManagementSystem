package com.karate.authservice.unit.service;

import com.karate.authservice.api.dto.*;
import com.karate.authservice.domain.exception.InvalidUserCredentialsException;
import com.karate.authservice.domain.exception.UserNotFoundException;
import com.karate.authservice.domain.exception.UsernameWhileTryingToLogInNotFoundException;
import com.karate.authservice.domain.model.AuthUserEntity;
import com.karate.authservice.domain.model.RoleEntity;
import com.karate.authservice.domain.model.RoleName;
import com.karate.authservice.domain.model.dto.UserDto;
import com.karate.authservice.domain.repository.AuthUserRepository;
import com.karate.authservice.domain.repository.RoleRepository;
import com.karate.authservice.domain.service.AuthService;
import com.karate.authservice.domain.service.UpstreamGateway;
import com.karate.authservice.infrastructure.client.dto.KarateClubDto;
import com.karate.authservice.infrastructure.client.dto.UserInfoDto;
import com.karate.authservice.infrastructure.messaging.UserEventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    AuthUserRepository authUserRepository;
    @Mock
    RoleRepository roleRepository;
    @Mock
    UserEventProducer userEventProducer;
    @Mock
    UpstreamGateway upstream;

    @InjectMocks
    AuthService service;

    private RoleEntity roleUser;

    @BeforeEach
    void setUp() {
        roleUser = new RoleEntity();
        roleUser.setRoleId(1L);
        roleUser.setName(RoleName.ROLE_USER);
    }

    @Test
    void register_happyPath_persists_authuser_links_callsUpstreams_andPublishesEvent() {
        // given
        var req = RegisterUserDto.builder()
                .username("john")
                .email("j@ex.com")
                .address(new AddressRequestDto("C", "S", "1", "00-000"))
                .karateClubName("TOKYO")
                .karateRank("KYU_9")
                .role("USER")
                .password("ENC")
                .build();

        var club = new KarateClubDto(21L, "TOKYO");
        var savedAuth = AuthUserEntity.builder()
                .authUserId(501L)
                .username("john")
                .password("ENC")
                .roleEntities(new HashSet<>(Set.of(roleUser)))
                .build();
        var linked = AuthUserEntity.builder()
                .authUserId(501L)
                .userId(777L)
                .username("john")
                .password("ENC")
                .roleEntities(new HashSet<>(Set.of(roleUser)))
                .build();

        when(upstream.getClubByName("TOKYO")).thenReturn(club);
        when(roleRepository.findByName(RoleName.ROLE_USER)).thenReturn(Optional.of(roleUser));
        when(upstream.createUserAsync(any())).thenReturn(java.util.concurrent.CompletableFuture.completedFuture(777L));
        when(authUserRepository.save(any(AuthUserEntity.class))).thenReturn(savedAuth, linked);
        when(upstream.getUserById(777L)).thenReturn(new UserInfoDto(777L, "j@ex.com", 21L, "KYU_9"));

        // when
        RegistrationResultDto res = service.register(req);

        // then
        assertThat(res.userId()).isEqualTo(777L);
        assertThat(res.username()).isEqualTo("john");
        assertThat(res.email()).isEqualTo("j@ex.com");

        verify(userEventProducer).sendUserRegisteredEvent(argThat(ev ->
                ev.getEventType().equals("USER_REGISTERED")
                        && ev.getPayload().getUserId().equals(777L)
                        && ev.getPayload().getUsername().equals("john")
                        && ev.getPayload().getClubId().equals(21L)
                        && ev.getPayload().getClubName().equals("TOKYO")
                        && ev.getPayload().getUserEmail().equals("j@ex.com")
                        && ev.getPayload().getKarateRank().equals("KYU_9")
        ));
    }

    @Test
    void register_throws_whenInvalidKarateRank() {
        // given
        var req = RegisterUserDto.builder()
                .username("john").email("j@ex.com")
                .address(new AddressRequestDto("C", "S", "1", "00-000"))
                .karateClubName("TOKYO")
                .karateRank("NOPE")
                .role("USER").password("ENC").build();

        // when // then
        assertThatThrownBy(() -> service.register(req))
                .isInstanceOf(InvalidUserCredentialsException.class)
                .hasMessageContaining("Invalid Karate Rank");
    }

    @Test
    void register_throws_whenInvalidRole() {
        // given
        var req = RegisterUserDto.builder()
                .username("john").email("j@ex.com")
                .address(new AddressRequestDto("C", "S", "1", "00-000"))
                .karateClubName("TOKYO")
                .karateRank("KYU_9")
                .role("nope").password("ENC").build();

        // when // then
        assertThatThrownBy(() -> service.register(req))
                .isInstanceOf(InvalidUserCredentialsException.class)
                .hasMessageContaining("Invalid Role");
    }

    @Test
    void register_throws_whenMandatoryFieldIsNull() {
        // given
        var req = RegisterUserDto.builder()
                .username(null)
                .email("j@ex.com")
                .address(new AddressRequestDto("C", "S", "1", "00-000"))
                .karateClubName("TOKYO")
                .karateRank("KYU_9")
                .role("USER")
                .password("ENC").build();

        // when // then
        assertThatThrownBy(() -> service.register(req))
                .isInstanceOf(InvalidUserCredentialsException.class)
                .hasMessageContaining("cannot be null");
    }

    @Test
    void findByUsername_mapsToUserDto_andFetchesClub() {
        // given
        var entity = AuthUserEntity.builder()
                .authUserId(1L)
                .userId(777L)
                .username("john")
                .password("ENC")
                .roleEntities(new HashSet<>(Set.of(roleUser)))
                .build();

        when(authUserRepository.findByUsername("john")).thenReturn(Optional.of(entity));
        when(upstream.getUserById(777L)).thenReturn(new UserInfoDto(777L, "j@ex.com", 21L, "KYU_9"));
        when(upstream.getClubById(21L)).thenReturn(new KarateClubDto(21L, "TOKYO"));

        // when
        UserDto dto = service.findByUsername("john");

        // then
        assertThat(dto.username()).isEqualTo("john");
        assertThat(dto.karateClubName()).isEqualTo("TOKYO");
        assertThat(dto.roles()).contains(RoleName.ROLE_USER);
        assertThat(dto.password()).isEqualTo("ENC");
    }

    @Test
    void findByUsername_throws_whenNotFound() {
        // given
        when(authUserRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        // when // then
        assertThatThrownBy(() -> service.findByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void validateUserForLogin_ok_whenClubMatches() {
        // given
        var spy = Mockito.spy(service);
        doReturn(new UserDto(1L, "john", "ENC", Set.of(RoleName.ROLE_USER), "TOKYO"))
                .when(spy).findByUsername("john");
        var req = TokenRequestDto.builder().username("john").password("pw").karateClubName("TOKYO").build();

        // when // then
        assertThatCode(() -> spy.validateUserForLogin(req)).doesNotThrowAnyException();
    }

    @Test
    void validateUserForLogin_throws_whenClubMismatch() {
        // given
        var spy = Mockito.spy(service);
        doReturn(new UserDto(1L, "john", "ENC", Set.of(RoleName.ROLE_USER), "OSAKA"))
                .when(spy).findByUsername("john");
        var req = TokenRequestDto.builder().username("john").password("pw").karateClubName("TOKYO").build();

        // when // then
        assertThatThrownBy(() -> spy.validateUserForLogin(req))
                .isInstanceOf(UsernameWhileTryingToLogInNotFoundException.class);
    }

    @Test
    void validateUserForLogin_throws_whenUsernameUnknown() {
        // given
        var spy = Mockito.spy(service);
        doThrow(new UsernameNotFoundException("nope")).when(spy).findByUsername("ghost");
        var req = TokenRequestDto.builder().username("ghost").password("pw").karateClubName("TOKYO").build();

        // when // then
        assertThatThrownBy(() -> spy.validateUserForLogin(req))
                .isInstanceOf(UsernameWhileTryingToLogInNotFoundException.class);
    }

    @Test
    void getAuthUserDto_returnsDto() {
        // given
        var entity = AuthUserEntity.builder()
                .authUserId(1L).userId(88L).username("john")
                .roleEntities(new HashSet<>(Set.of(roleUser)))
                .password("ENC").build();
        when(authUserRepository.findByUserId(88L)).thenReturn(Optional.of(entity));

        // when
        AuthUserDto dto = service.getAuthUserDto(88L);

        // then
        assertThat(dto.userId()).isEqualTo(88L);
        assertThat(dto.username()).isEqualTo("john");
        assertThat(dto.roles()).contains("ROLE_USER");
    }

    @Test
    void getAuthUserDtoByUsername_returnsDto() {
        // given
        var entity = AuthUserEntity.builder()
                .authUserId(1L).userId(99L).username("mary")
                .roleEntities(new HashSet<>(Set.of(roleUser)))
                .password("ENC").build();
        when(authUserRepository.findByUsername("mary")).thenReturn(Optional.of(entity));

        // when
        AuthUserDto dto = service.getAuthUserDtoByUsername("mary");

        // then
        assertThat(dto.userId()).isEqualTo(99L);
        assertThat(dto.username()).isEqualTo("mary");
        assertThat(dto.roles()).contains("ROLE_USER");
    }

    @Test
    void getUsername_returnsValue_orThrows() {
        // given
        when(authUserRepository.findByUserId(1L)).thenReturn(Optional.of(
                AuthUserEntity.builder().userId(1L).username("neo").password("x").build()
        ));

        // when // then
        assertThat(service.getUsername(1L)).isEqualTo("neo");

        // and: not found
        when(authUserRepository.findByUserId(2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getUsername(2L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getUserIdByUsername_returnsValue_orThrows() {
        // given
        when(authUserRepository.findByUsername("john")).thenReturn(Optional.of(
                AuthUserEntity.builder().userId(777L).username("john").password("x").build()
        ));
        when(authUserRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        // when // then
        assertThat(service.getUserIdByUsername("john")).isEqualTo(777L);
        assertThatThrownBy(() -> service.getUserIdByUsername("ghost"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void updateUsername_updatesAndSaves_orThrows() {
        // given
        var e = AuthUserEntity.builder().userId(5L).username("old").password("x").build();
        when(authUserRepository.findByUserId(5L)).thenReturn(Optional.of(e));

        // when
        service.updateUsername(5L, "newName");

        // then
        assertThat(e.getUsername()).isEqualTo("newName");
        verify(authUserRepository).save(e);

        // not found branch
        when(authUserRepository.findByUserId(6L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.updateUsername(6L, "x"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void deleteUser_deletes_orThrows() {
        // given
        var e = AuthUserEntity.builder().userId(7L).username("x").password("p").build();
        when(authUserRepository.findByUserId(7L)).thenReturn(Optional.of(e));

        // when
        service.deleteUser(7L);

        // then
        verify(authUserRepository).delete(e);

        // not found branch
        when(authUserRepository.findByUserId(8L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.deleteUser(8L))
                .isInstanceOf(UserNotFoundException.class);
    }
}

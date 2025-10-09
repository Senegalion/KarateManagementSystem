package com.karate.authservice.domain.service;

import com.karate.authservice.api.dto.AuthUserDto;
import com.karate.authservice.api.dto.RegisterUserDto;
import com.karate.authservice.api.dto.RegistrationResultDto;
import com.karate.authservice.api.dto.TokenRequestDto;
import com.karate.authservice.domain.exception.InvalidUserCredentialsException;
import com.karate.authservice.domain.exception.UserNotFoundException;
import com.karate.authservice.domain.exception.UsernameWhileTryingToLogInNotFoundException;
import com.karate.authservice.domain.model.AuthUserEntity;
import com.karate.authservice.domain.model.KarateRank;
import com.karate.authservice.domain.model.RoleEntity;
import com.karate.authservice.domain.model.RoleName;
import com.karate.authservice.domain.model.dto.UserDto;
import com.karate.authservice.domain.repository.AuthUserRepository;
import com.karate.authservice.domain.repository.RoleRepository;
import com.karate.authservice.infrastructure.client.dto.AddressDto;
import com.karate.authservice.infrastructure.client.dto.KarateClubDto;
import com.karate.authservice.infrastructure.client.dto.NewUserRequestDto;
import com.karate.authservice.infrastructure.client.dto.UserInfoDto;
import com.karate.authservice.infrastructure.messaging.UserEventProducer;
import com.karate.authservice.infrastructure.messaging.dto.UserRegisteredEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class AuthService {
    private final AuthUserRepository authUserRepository;
    private final RoleRepository roleRepository;
    private final UserEventProducer userEventProducer;
    private final UpstreamGateway upstream;

    @Transactional
    public RegistrationResultDto register(RegisterUserDto registerUserDto) {
        log.info("Register user={} role={} club={}", registerUserDto.username(), registerUserDto.role(), registerUserDto.karateClubName());
        validateRegistrationData(registerUserDto);

        KarateClubDto club = resolveClub(registerUserDto.karateClubName());
        RoleEntity role = resolveRole(registerUserDto.role());

        AuthUserEntity authUser = persistAuthUser(registerUserDto.username(), registerUserDto.password(), role);

        Long userId = createUserInUserService(authUser.getAuthUserId(), registerUserDto, club);

        AuthUserEntity linked = linkAuthToUser(authUser, userId);

        UserInfoDto userInfo = fetchUserInfo(userId);

        publishUserRegistered(linked, userInfo, club);

        return buildRegistrationResult(linked, registerUserDto);
    }

    private static void validateWhetherRegistrationDataAreNull(RegisterUserDto registerUserDto) {
        if (registerUserDto.username() == null || registerUserDto.email() == null
                || registerUserDto.password() == null || registerUserDto.karateClubName() == null
                || registerUserDto.karateRank() == null || registerUserDto.role() == null) {
            log.debug("Registration validation failed: some mandatory fields are null for user={}", registerUserDto.username());
            throw new InvalidUserCredentialsException("User data cannot be null");
        }
    }

    private void validateRegistrationData(RegisterUserDto registerUserDto) {
        validateWhetherRegistrationDataAreNull(registerUserDto);

        validateKarateRank(registerUserDto.karateRank());
        validateRole(registerUserDto.role());
        log.debug("Registration validation OK user={}", registerUserDto.username());
    }

    private void validateKarateRank(String karateRank) {
        try {
            KarateRank.valueOf(karateRank);
        } catch (IllegalArgumentException e) {
            log.debug("Invalid karateRank={}", karateRank);
            throw new InvalidUserCredentialsException(String.format("Invalid Karate Rank: [%s]", karateRank));
        }
    }

    private void validateRole(String role) {
        try {
            RoleName.valueOf("ROLE_" + role.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.debug("Invalid role={}", role);
            throw new InvalidUserCredentialsException(String.format("Invalid Role: [%s]", role));
        }
    }

    @Transactional
    public UserDto findByUsername(String username) {
        log.debug("findByUsername username={}", username);
        AuthUserEntity authUserEntity = authUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Set<RoleName> roles = authUserEntity.getRoleEntities().stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toSet());

        long tUsr0 = System.currentTimeMillis();
        UserInfoDto userInfo = upstream.getUserById(authUserEntity.getUserId());
        long tClub0 = System.currentTimeMillis();
        KarateClubDto karateClub = upstream.getClubById(userInfo.karateClubId());
        log.debug("findByUsername fetched details userId={} userInfoTook={}ms clubTook={}ms",
                authUserEntity.getUserId(),
                tClub0 - tUsr0,
                System.currentTimeMillis() - tClub0);

        return new UserDto(
                authUserEntity.getUserId(),
                authUserEntity.getUsername(),
                authUserEntity.getPassword(),
                roles,
                karateClub.name()
        );
    }

    public AuthUserEntity findByUserId(Long userId) {
        log.debug("findByUserId userId={}", userId);
        return authUserRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Auth user not found"));
    }

    @Transactional(readOnly = true)
    public void validateUserForLogin(TokenRequestDto tokenRequestDto) {
        String username = tokenRequestDto.username();
        log.info("Login validation start user={} club={}", username, tokenRequestDto.karateClubName());
        try {
            UserDto user = findByUsername(username);
            boolean clubMatch = user.karateClubName().equalsIgnoreCase(tokenRequestDto.karateClubName());
            log.debug("Login validation user={} clubMatch={}", username, clubMatch);
            if (!clubMatch) {
                throw new UsernameWhileTryingToLogInNotFoundException("Invalid club for this user");
            }
            log.info("Login validation OK user={}", username);
        } catch (UsernameNotFoundException e) {
            log.warn("Login attempt for non-existing user={}", username);
            throw new UsernameWhileTryingToLogInNotFoundException("Invalid username or password");
        }
    }

    @Transactional(readOnly = true)
    public AuthUserDto getAuthUserDto(Long userId) {
        log.debug("getAuthUserDto userId={}", userId);
        AuthUserEntity entity = findByUserId(userId);
        return new AuthUserDto(
                entity.getUserId(),
                entity.getUsername(),
                entity.getRoleEntities().stream()
                        .map(r -> r.getName().name())
                        .collect(Collectors.toSet())
        );
    }

    @Transactional(readOnly = true)
    public AuthUserDto getAuthUserDtoByUsername(String username) {
        log.debug("getAuthUserDtoByUsername username={}", username);
        AuthUserEntity user = authUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new AuthUserDto(
                user.getUserId(),
                user.getUsername(),
                user.getRoleEntities().stream()
                        .map(r -> r.getName().name())
                        .collect(Collectors.toSet())
        );
    }

    @Transactional(readOnly = true)
    public String getUsername(Long userId) {
        log.debug("getUsername userId={}", userId);
        AuthUserEntity user = authUserRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return user.getUsername();
    }

    @Transactional(readOnly = true)
    public Long getUserIdByUsername(String username) {
        log.debug("getUserIdByUsername username={}", username);
        AuthUserEntity user = authUserRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UserNotFoundException("User with username: [" + username + "] not found")
                );
        return user.getUserId();
    }

    @Transactional
    public void updateUsername(Long userId, String newUsername) {
        log.info("Update username userId={} newUsername={}", userId, newUsername);
        AuthUserEntity user = authUserRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setUsername(newUsername);
        authUserRepository.save(user);
        log.info("Update username OK userId={}", userId);
    }

    @Transactional
    public void deleteUser(Long userId) {
        log.info("Delete user userId={}", userId);
        AuthUserEntity user = authUserRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        authUserRepository.delete(user);
        log.info("Delete user OK userId={}", userId);
    }

    private KarateClubDto resolveClub(String clubName) {
        long t0 = System.currentTimeMillis();
        KarateClubDto club = upstream.getClubByName(clubName);
        log.debug("Club resolved name='{}' -> took={}ms", clubName, System.currentTimeMillis() - t0);
        return club;
    }

    private RoleEntity resolveRole(String roleRaw) {
        return roleRepository.findByName(RoleName.valueOf("ROLE_" + roleRaw.toUpperCase()))
                .orElseThrow(() -> new InvalidUserCredentialsException("Role not found"));
    }

    private AuthUserEntity persistAuthUser(String username, String rawPassword, RoleEntity role) {
        AuthUserEntity entity = AuthUserEntity.builder()
                .username(username)
                .password(rawPassword)
                .roleEntities(new HashSet<>(Set.of(role)))
                .build();

        long t0 = System.currentTimeMillis();
        AuthUserEntity saved = authUserRepository.save(entity);
        log.info("Auth user persisted authUserId={} username={} took={}ms",
                saved.getAuthUserId(), saved.getUsername(), System.currentTimeMillis() - t0);
        return saved;
    }

    private Long createUserInUserService(Long authUserId, RegisterUserDto dto, KarateClubDto club) {
        NewUserRequestDto payload = new NewUserRequestDto(
                authUserId,
                dto.email(),
                club.karateClubId(),
                dto.karateRank(),
                new AddressDto(
                        dto.address().city(),
                        dto.address().street(),
                        dto.address().number(),
                        dto.address().postalCode()
                )
        );

        long t0 = System.currentTimeMillis();
        Long userId = upstream.createUserAsync(payload).join();
        log.info("user-service createUser userId={} took={}ms", userId, System.currentTimeMillis() - t0);
        return userId;
    }

    private AuthUserEntity linkAuthToUser(AuthUserEntity authUser, Long userId) {
        authUser.setUserId(userId);
        long t0 = System.currentTimeMillis();
        AuthUserEntity saved = authUserRepository.save(authUser);
        log.debug("Linked authUserId={} -> userId={} took={}ms",
                saved.getAuthUserId(), saved.getUserId(), System.currentTimeMillis() - t0);
        return saved;
    }

    private UserInfoDto fetchUserInfo(Long userId) {
        long t0 = System.currentTimeMillis();
        UserInfoDto info = upstream.getUserById(userId);
        log.debug("user-service getUserById userId={} took={}ms", userId, System.currentTimeMillis() - t0);
        return info;
    }

    private void publishUserRegistered(AuthUserEntity user, UserInfoDto info, KarateClubDto club) {
        var event = new UserRegisteredEvent(
                UUID.randomUUID().toString(),
                "USER_REGISTERED",
                Instant.now(),
                new UserRegisteredEvent.Payload(
                        user.getUserId(),
                        info.email(),
                        user.getUsername(),
                        club.karateClubId(),
                        club.name(),
                        info.karateRank(),
                        info.registrationDate()
                )
        );
        userEventProducer.sendUserRegisteredEvent(event);
        log.info("UserRegisteredEvent sent userId={} eventId={}", user.getUserId(), event.getEventId());
    }

    private RegistrationResultDto buildRegistrationResult(AuthUserEntity saved, RegisterUserDto dto) {
        return RegistrationResultDto.builder()
                .userId(saved.getUserId())
                .username(dto.username())
                .email(dto.email())
                .build();
    }
}

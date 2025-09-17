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
import com.karate.authservice.infrastructure.client.KarateClubClient;
import com.karate.authservice.infrastructure.client.UserClient;
import com.karate.authservice.infrastructure.client.dto.AddressDto;
import com.karate.authservice.infrastructure.client.dto.KarateClubDto;
import com.karate.authservice.infrastructure.client.dto.NewUserRequestDto;
import com.karate.authservice.infrastructure.client.dto.UserInfoDto;
import com.karate.authservice.infrastructure.messaging.UserEventProducer;
import com.karate.authservice.infrastructure.messaging.event.UserRegisteredEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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
    private final KarateClubClient karateClubClient;
    private final UserClient userClient;
    private final UserEventProducer userEventProducer;

    @Transactional
    public RegistrationResultDto register(RegisterUserDto registerUserDto) {
        log.info("Register user={} role={} club={}", registerUserDto.username(), registerUserDto.role(), registerUserDto.karateClubName());
        validateRegistrationData(registerUserDto);

        long tCl0 = System.currentTimeMillis();
        KarateClubDto karateClubDto = karateClubClient.getClubByName(registerUserDto.karateClubName());
        log.debug("Club resolved name='{}' -> took={}ms", registerUserDto.karateClubName(), System.currentTimeMillis() - tCl0);

        RoleEntity roleEntity = roleRepository.findByName(RoleName.valueOf("ROLE_" + registerUserDto.role().toUpperCase()))
                .orElseThrow(() -> new InvalidUserCredentialsException("Role not found"));

        AuthUserEntity authUser = AuthUserEntity.builder()
                .username(registerUserDto.username())
                .password(registerUserDto.password())
                .roleEntities(new HashSet<>(Set.of(roleEntity)))
                .build();
        long tDb0 = System.currentTimeMillis();
        AuthUserEntity savedAuthUser = authUserRepository.save(authUser);
        log.info("Auth user persisted authUserId={} username={} took={}ms",
                savedAuthUser.getAuthUserId(), savedAuthUser.getUsername(), System.currentTimeMillis() - tDb0);

        NewUserRequestDto newUserRequest = new NewUserRequestDto(
                savedAuthUser.getAuthUserId(),
                registerUserDto.email(),
                karateClubDto.karateClubId(),
                registerUserDto.karateRank(),
                new AddressDto(
                        registerUserDto.address().city(),
                        registerUserDto.address().street(),
                        registerUserDto.address().number(),
                        registerUserDto.address().postalCode()
                )
        );

        long tUsr0 = System.currentTimeMillis();
        Long createdUserId = userClient.createUser(newUserRequest);
        log.info("user-service createUser userId={} took={}ms", createdUserId, System.currentTimeMillis() - tUsr0);

        savedAuthUser.setUserId(createdUserId);
        long tDb1 = System.currentTimeMillis();
        AuthUserEntity saved = authUserRepository.save(savedAuthUser);
        log.debug("Linked authUserId={} -> userId={} took={}ms", saved.getAuthUserId(), saved.getUserId(),
                System.currentTimeMillis() - tDb1);

        long tUsr1 = System.currentTimeMillis();
        UserInfoDto userInfoDto = userClient.getUserById(saved.getUserId());
        log.debug("user-service getUserById userId={} took={}ms", saved.getUserId(), System.currentTimeMillis() - tUsr1);

        UserRegisteredEvent event = new UserRegisteredEvent(
                UUID.randomUUID().toString(),
                "USER_REGISTERED",
                Instant.now(),
                new UserRegisteredEvent.Payload(
                        saved.getUserId(),
                        userInfoDto.email(),
                        saved.getUsername(),
                        karateClubDto.karateClubId(),
                        karateClubDto.name(),
                        userInfoDto.karateRank()
                )
        );

        userEventProducer.sendUserRegisteredEvent(event);
        log.info("UserRegisteredEvent sent userId={} eventId={}", saved.getUserId(), event.getEventId());

        return RegistrationResultDto.builder()
                .userId(savedAuthUser.getUserId())
                .username(registerUserDto.username())
                .email(registerUserDto.email())
                .build();
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
        UserInfoDto userInfo = userClient.getUserById(authUserEntity.getUserId());
        long tClub0 = System.currentTimeMillis();
        KarateClubDto karateClub = karateClubClient.getClubById(userInfo.karateClubId());
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
}

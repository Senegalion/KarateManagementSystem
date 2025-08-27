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
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AuthService {
    private final AuthUserRepository authUserRepository;
    private final RoleRepository roleRepository;
    private final KarateClubClient karateClubClient;
    private final UserClient userClient;

    @Transactional
    public RegistrationResultDto register(RegisterUserDto registerUserDto) {
        validateRegistrationData(registerUserDto);

        KarateClubDto karateClubDto = karateClubClient.getClubByName(registerUserDto.karateClubName());

        RoleEntity roleEntity = roleRepository.findByName(RoleName.valueOf("ROLE_" + registerUserDto.role().toUpperCase()))
                .orElseThrow(() -> new InvalidUserCredentialsException("Role not found"));

        AuthUserEntity authUser = AuthUserEntity.builder()
                .username(registerUserDto.username())
                .password(registerUserDto.password())
                .roleEntities(new HashSet<>(Set.of(roleEntity)))
                .build();
        AuthUserEntity savedAuthUser = authUserRepository.save(authUser);

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

        Long createdUserId = userClient.createUser(newUserRequest);

        savedAuthUser.setUserId(createdUserId);
        authUserRepository.save(savedAuthUser);

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
            throw new InvalidUserCredentialsException("User data cannot be null");
        }
    }

    private void validateRegistrationData(RegisterUserDto registerUserDto) {
        validateWhetherRegistrationDataAreNull(registerUserDto);

        validateKarateRank(registerUserDto.karateRank());
        validateRole(registerUserDto.role());
    }

    private void validateKarateRank(String karateRank) {
        try {
            KarateRank.valueOf(karateRank);
        } catch (IllegalArgumentException e) {
            throw new InvalidUserCredentialsException(String.format("Invalid Karate Rank: [%s]", karateRank));
        }
    }

    private void validateRole(String role) {
        try {
            RoleName.valueOf("ROLE_" + role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidUserCredentialsException(String.format("Invalid Role: [%s]", role));
        }
    }

    @Transactional
    public UserDto findByUsername(String username) {
        AuthUserEntity authUserEntity = authUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Set<RoleName> roles = authUserEntity.getRoleEntities().stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toSet());

        UserInfoDto userInfo = userClient.getUserById(authUserEntity.getUserId());
        KarateClubDto karateClub = karateClubClient.getClubById(userInfo.karateClubId());

        return new UserDto(
                authUserEntity.getUserId(),
                authUserEntity.getUsername(),
                authUserEntity.getPassword(),
                roles,
                karateClub.name()
        );
    }

    public AuthUserEntity findByUserId(Long userId) {
        return authUserRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Auth user not found"));
    }

    @Transactional(readOnly = true)
    public void validateUserForLogin(TokenRequestDto tokenRequestDto) {
        UserDto user;
        try {
            user = findByUsername(tokenRequestDto.username());
        } catch (UsernameNotFoundException e) {
            throw new UsernameWhileTryingToLogInNotFoundException("Invalid username or password");
        }

        if (!user.karateClubName().equalsIgnoreCase(tokenRequestDto.karateClubName())) {
            throw new UsernameWhileTryingToLogInNotFoundException("Invalid club for this user");
        }
    }

    @Transactional(readOnly = true)
    public AuthUserDto getAuthUserDto(Long userId) {
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
        AuthUserEntity user = authUserRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return user.getUsername();
    }
}

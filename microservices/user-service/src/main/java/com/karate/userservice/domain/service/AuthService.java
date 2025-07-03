package com.karate.userservice.domain.service;

import com.karate.userservice.api.dto.RegisterUserDto;
import com.karate.userservice.api.dto.RegistrationResultDto;
import com.karate.userservice.domain.exception.InvalidUserCredentialsException;
import com.karate.userservice.domain.model.KarateRank;
import com.karate.userservice.domain.model.RoleEntity;
import com.karate.userservice.domain.model.RoleName;
import com.karate.userservice.domain.model.UserEntity;
import com.karate.userservice.domain.model.dto.UserDto;
import com.karate.userservice.domain.repository.RoleRepository;
import com.karate.userservice.domain.repository.UserRepository;
import com.karate.userservice.infrastructure.client.KarateClubClient;
import com.karate.userservice.infrastructure.client.dto.KarateClubDto;
import com.karate.userservice.infrastructure.persistence.mapper.UserMapper;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final KarateClubClient karateClubClient;

    @Transactional
    public RegistrationResultDto register(RegisterUserDto registerUserDto) {
        validateRegistrationData(registerUserDto);

        KarateClubDto karateClubDto = karateClubClient.getClubByName(registerUserDto.karateClubName());

        RoleEntity roleEntity = roleRepository.findByName(RoleName.valueOf("ROLE_" + registerUserDto.role().toUpperCase()))
                .orElseThrow(() -> new InvalidUserCredentialsException("Role not found"));

        UserEntity user = UserMapper.mapFromUserDto(registerUserDto, karateClubDto.karateClubId(), roleEntity);
        user.setKarateClubId(karateClubDto.karateClubId());
        user.getRoleEntities().add(roleEntity);

        UserEntity savedUser = userRepository.save(user);

        return RegistrationResultDto.builder()
                .userId(savedUser.getUserId())
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
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Set<RoleName> roles = userEntity.getRoleEntities().stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toSet());

        KarateClubDto karateClub = karateClubClient.getClubById(userEntity.getKarateClubId());

        return new UserDto(
                userEntity.getUserId(),
                userEntity.getUsername(),
                userEntity.getPassword(),
                roles,
                karateClub.name()
        );
    }
}

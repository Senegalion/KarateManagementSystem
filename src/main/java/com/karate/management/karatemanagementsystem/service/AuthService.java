package com.karate.management.karatemanagementsystem.service;

import com.karate.management.karatemanagementsystem.model.staticdata.KarateClubName;
import com.karate.management.karatemanagementsystem.model.staticdata.KarateRank;
import com.karate.management.karatemanagementsystem.model.staticdata.RoleName;
import com.karate.management.karatemanagementsystem.model.dto.registration.RegisterUserDto;
import com.karate.management.karatemanagementsystem.model.dto.registration.RegistrationResultDto;
import com.karate.management.karatemanagementsystem.model.dto.user.UserDto;
import com.karate.management.karatemanagementsystem.model.entity.KarateClubEntity;
import com.karate.management.karatemanagementsystem.model.entity.RoleEntity;
import com.karate.management.karatemanagementsystem.model.entity.UserEntity;
import com.karate.management.karatemanagementsystem.model.repository.KarateClubRepository;
import com.karate.management.karatemanagementsystem.model.repository.RoleRepository;
import com.karate.management.karatemanagementsystem.model.repository.UserRepository;
import com.karate.management.karatemanagementsystem.service.exception.InvalidUserCredentialsException;
import com.karate.management.karatemanagementsystem.service.mapper.UserMapper;
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
    private final KarateClubRepository karateClubRepository;
    private final RoleRepository roleRepository;

    @Transactional
    public RegistrationResultDto register(RegisterUserDto registerUserDto) {
        validateRegistrationData(registerUserDto);

        KarateClubEntity karateClub = karateClubRepository.findByName(KarateClubName.valueOf(registerUserDto.karateClubName()))
                .orElseThrow(() -> new InvalidUserCredentialsException("Karate club not found"));

        RoleEntity roleEntity = roleRepository.findByName(RoleName.valueOf("ROLE_" + registerUserDto.role().toUpperCase()))
                .orElseThrow(() -> new InvalidUserCredentialsException("Role not found"));

        UserMapper userMapper = new UserMapper(karateClubRepository, roleRepository);
        UserEntity user = userMapper.mapFromUserDto(registerUserDto);
        user.setKarateClub(karateClub);

        user.getRoleEntities().add(roleEntity);

        UserEntity savedUser = userRepository.save(user);

        return RegistrationResultDto.builder()
                .userId(savedUser.getUserId())
                .username(registerUserDto.username())
                .build();
    }

    private static void validateWhetherRegistrationDataAreNull(RegisterUserDto registerUserDto) {
        if (registerUserDto.username() == null || registerUserDto.password() == null
                || registerUserDto.karateClubName() == null || registerUserDto.karateRank() == null
                || registerUserDto.role() == null) {
            throw new InvalidUserCredentialsException("User data cannot be null");
        }
    }

    private void validateRegistrationData(RegisterUserDto registerUserDto) {
        validateWhetherRegistrationDataAreNull(registerUserDto);

        validateKarateClubName(registerUserDto.karateClubName());
        validateKarateRank(registerUserDto.karateRank());
        validateRole(registerUserDto.role());
    }

    private void validateKarateClubName(String karateClubName) {
        try {
            KarateClubName.valueOf(karateClubName);
        } catch (IllegalArgumentException e) {
            throw new InvalidUserCredentialsException(String.format("Invalid Karate Club Name: [%s]", karateClubName));
        }
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

        return new UserDto(
                userEntity.getUserId(),
                userEntity.getUsername(),
                userEntity.getPassword(),
                roles
        );
    }
}

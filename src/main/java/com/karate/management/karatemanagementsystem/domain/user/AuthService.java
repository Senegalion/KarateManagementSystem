package com.karate.management.karatemanagementsystem.domain.user;

import com.karate.management.karatemanagementsystem.domain.user.dto.RegisterUserDto;
import com.karate.management.karatemanagementsystem.domain.user.dto.RegistrationResultDto;
import com.karate.management.karatemanagementsystem.domain.user.dto.UserDto;
import com.karate.management.karatemanagementsystem.domain.user.KarateClubEntity;
import com.karate.management.karatemanagementsystem.domain.user.RoleEntity;
import com.karate.management.karatemanagementsystem.domain.user.UserEntity;
import com.karate.management.karatemanagementsystem.domain.user.KarateClubRepository;
import com.karate.management.karatemanagementsystem.domain.user.RoleRepository;
import com.karate.management.karatemanagementsystem.domain.user.UserRepository;
import com.karate.management.karatemanagementsystem.domain.user.KarateClubName;
import com.karate.management.karatemanagementsystem.domain.user.KarateRank;
import com.karate.management.karatemanagementsystem.domain.user.RoleName;
import com.karate.management.karatemanagementsystem.domain.user.InvalidUserCredentialsException;
import com.karate.management.karatemanagementsystem.domain.user.UserMapper;
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

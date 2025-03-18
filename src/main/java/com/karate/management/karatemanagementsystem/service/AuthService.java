package com.karate.management.karatemanagementsystem.service;

import com.karate.management.karatemanagementsystem.model.data.KarateClubName;
import com.karate.management.karatemanagementsystem.model.data.KarateRank;
import com.karate.management.karatemanagementsystem.model.data.RoleName;
import com.karate.management.karatemanagementsystem.model.dto.RegisterUserDto;
import com.karate.management.karatemanagementsystem.model.dto.RegistrationResultDto;
import com.karate.management.karatemanagementsystem.model.dto.UserDto;
import com.karate.management.karatemanagementsystem.model.entity.UserEntity;
import com.karate.management.karatemanagementsystem.model.repository.KarateClubRepository;
import com.karate.management.karatemanagementsystem.model.repository.UserRepository;
import com.karate.management.karatemanagementsystem.service.exception.InvalidUserCredentialsException;
import com.karate.management.karatemanagementsystem.service.mapper.UserMapper;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final KarateClubRepository karateClubRepository;

    public RegistrationResultDto register(RegisterUserDto registerUserDto) {
        validateRegistrationData(registerUserDto);

        UserMapper userMapper = new UserMapper(karateClubRepository);
        UserEntity user = userMapper.mapFromUserDto(registerUserDto);
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
            RoleName.valueOf(role);
        } catch (IllegalArgumentException e) {
            throw new InvalidUserCredentialsException(String.format("Invalid Role: [%s]", role));
        }
    }

    public UserDto findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(user -> new UserDto(user.getUserId(), user.getUsername(), user.getPassword()))
                .orElseThrow(() -> new BadCredentialsException("User not found"));
    }
}

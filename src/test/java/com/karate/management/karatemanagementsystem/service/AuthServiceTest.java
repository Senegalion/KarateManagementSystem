package com.karate.management.karatemanagementsystem.service;

import com.karate.management.karatemanagementsystem.model.dto.RegisterUserDto;
import com.karate.management.karatemanagementsystem.model.dto.RegistrationResultDto;
import com.karate.management.karatemanagementsystem.model.entity.UserEntity;
import com.karate.management.karatemanagementsystem.model.repository.UserRepository;
import com.karate.management.karatemanagementsystem.service.exception.InvalidUserCredentialsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUser_withValidData() {
        RegisterUserDto validDto = RegisterUserDto.builder()
                .username("testUser")
                .karateClubName("DOJO")
                .karateRank("BLACK")
                .role("USER")
                .password("password123")
                .build();

        UserEntity savedUser = new UserEntity();
        savedUser.setUserId(1L);
        savedUser.setUsername("testUser");

        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

        // When
        RegistrationResultDto result = authService.register(validDto);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.userId());
        assertEquals("testUser", result.username());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testRegisterUser_withNullUsername() {
        // Given
        RegisterUserDto invalidDto = RegisterUserDto.builder()
                .username(null)
                .karateClubName("DOJO")
                .karateRank("BLACK")
                .role("USER")
                .password("password123")
                .build();

        // When + Then
        InvalidUserCredentialsException exception = assertThrows(InvalidUserCredentialsException.class, () -> {
            authService.register(invalidDto);
        });
        assertEquals("User data cannot be null", exception.getMessage());
    }

    @Test
    void testRegisterUser_withInvalidKarateClubName() {
        // Given
        RegisterUserDto invalidDto = RegisterUserDto.builder()
                .username("testUser")
                .karateClubName("INVALID_CLUB")
                .karateRank("BLACK")
                .role("USER")
                .password("password123")
                .build();

        // When + Then
        InvalidUserCredentialsException exception = assertThrows(InvalidUserCredentialsException.class, () -> {
            authService.register(invalidDto);
        });
        assertEquals("Invalid Karate Club Name: [INVALID_CLUB]", exception.getMessage());
    }

    @Test
    void testRegisterUser_withInvalidKarateRank() {
        // Given
        RegisterUserDto invalidDto = RegisterUserDto.builder()
                .username("testUser")
                .karateClubName("DOJO")
                .karateRank("INVALID_RANK")
                .role("USER")
                .password("password123")
                .build();

        // When + Then
        InvalidUserCredentialsException exception = assertThrows(InvalidUserCredentialsException.class, () -> {
            authService.register(invalidDto);
        });
        assertEquals("Invalid Karate Rank: [INVALID_RANK]", exception.getMessage());
    }

    @Test
    void testRegisterUser_withInvalidRole() {
        // Given
        RegisterUserDto invalidDto = RegisterUserDto.builder()
                .username("testUser")
                .karateClubName("DOJO")
                .karateRank("BLACK")
                .role("INVALID_ROLE")
                .password("password123")
                .build();

        // When + Then
        InvalidUserCredentialsException exception = assertThrows(InvalidUserCredentialsException.class, () -> {
            authService.register(invalidDto);
        });
        assertEquals("Invalid Role: [INVALID_ROLE]", exception.getMessage());
    }

    @Test
    void testRegisterUser_withNullRole() {
        // Given
        RegisterUserDto invalidDto = RegisterUserDto.builder()
                .username("testUser")
                .karateClubName("DOJO")
                .karateRank("BLACK")
                .role(null)
                .password("password123")
                .build();

        // When + Then
        InvalidUserCredentialsException exception = assertThrows(InvalidUserCredentialsException.class, () -> {
            authService.register(invalidDto);
        });
        assertEquals("User data cannot be null", exception.getMessage());
    }

    @Test
    void testRegisterUser_withNullPassword() {
        // Given
        RegisterUserDto invalidDto = RegisterUserDto.builder()
                .username("testUser")
                .karateClubName("DOJO")
                .karateRank("BLACK")
                .role("USER")
                .password(null)
                .build();

        // When + Then
        InvalidUserCredentialsException exception = assertThrows(InvalidUserCredentialsException.class, () -> {
            authService.register(invalidDto);
        });
        assertEquals("User data cannot be null", exception.getMessage());
    }
}
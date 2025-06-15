package com.karate.management.karatemanagementsystem.service;

import com.karate.management.karatemanagementsystem.user.domain.exception.InvalidUserCredentialsException;
import com.karate.management.karatemanagementsystem.user.domain.model.KarateClubEntity;
import com.karate.management.karatemanagementsystem.user.domain.model.RoleEntity;
import com.karate.management.karatemanagementsystem.user.domain.model.UserEntity;
import com.karate.management.karatemanagementsystem.user.domain.repository.KarateClubRepository;
import com.karate.management.karatemanagementsystem.user.domain.repository.RoleRepository;
import com.karate.management.karatemanagementsystem.user.domain.repository.UserRepository;
import com.karate.management.karatemanagementsystem.user.domain.service.AuthService;
import com.karate.management.karatemanagementsystem.user.api.dto.RegisterUserDto;
import com.karate.management.karatemanagementsystem.user.api.dto.RegistrationResultDto;
import com.karate.management.karatemanagementsystem.user.domain.model.dto.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private KarateClubRepository karateClubRepository;

    @Mock
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUser_withValidData() {
        // given
        RegisterUserDto validDto = RegisterUserDto.builder()
                .username("testUser")
                .email("someEmail")
                .karateClubName("LODZKIE_CENTRUM_OKINAWA_SHORIN_RYU_KARATE_I_KOBUDO")
                .karateRank("KYU_10")
                .role("USER")
                .password("password123")
                .build();
        KarateClubEntity karateClubEntity = new KarateClubEntity();
        RoleEntity roleEntity = new RoleEntity();
        when(karateClubRepository.findByName(any())).thenReturn(Optional.of(karateClubEntity));
        when(roleRepository.findByName(any())).thenReturn(Optional.of(roleEntity));

        UserEntity savedUser = new UserEntity();
        savedUser.setUserId(1L);
        savedUser.setUsername("testUser");
        savedUser.setEmail("someEmail");
        savedUser.setRoleEntities(Set.of());
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

        // when
        RegistrationResultDto result = authService.register(validDto);

        // then
        assertNotNull(result);
        assertEquals(1L, result.userId());
        assertEquals("testUser", result.username());
        assertEquals("someEmail", result.email());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testRegisterUser_withNullUsername() {
        // given
        RegisterUserDto invalidDto = RegisterUserDto.builder()
                .username(null)
                .email("someEmail")
                .karateClubName("LODZKIE_CENTRUM_OKINAWA_SHORIN_RYU_KARATE_I_KOBUDO")
                .karateRank("KYU_10")
                .role("USER")
                .password("password123")
                .build();

        // when && then
        InvalidUserCredentialsException exception = assertThrows(InvalidUserCredentialsException.class, () -> {
            authService.register(invalidDto);
        });
        assertEquals("User data cannot be null", exception.getMessage());
    }

    @Test
    void testRegisterUser_withNullEmail() {
        // given
        RegisterUserDto invalidDto = RegisterUserDto.builder()
                .username("testUser")
                .email(null)
                .karateClubName("LODZKIE_CENTRUM_OKINAWA_SHORIN_RYU_KARATE_I_KOBUDO")
                .karateRank("KYU_10")
                .role("USER")
                .password("password123")
                .build();

        // when && then
        InvalidUserCredentialsException exception = assertThrows(InvalidUserCredentialsException.class, () -> {
            authService.register(invalidDto);
        });
        assertEquals("User data cannot be null", exception.getMessage());
    }

    @Test
    void testRegisterUser_withInvalidKarateClubName() {
        // given
        RegisterUserDto invalidDto = RegisterUserDto.builder()
                .username("testUser")
                .email("someEmail")
                .karateClubName("INVALID_CLUB")
                .karateRank("KYU_10")
                .role("USER")
                .password("password123")
                .build();

        // when && then
        InvalidUserCredentialsException exception = assertThrows(InvalidUserCredentialsException.class, () -> {
            authService.register(invalidDto);
        });
        assertEquals("Invalid Karate Club Name: [INVALID_CLUB]", exception.getMessage());
    }

    @Test
    void testRegisterUser_withInvalidKarateRank() {
        // given
        RegisterUserDto invalidDto = RegisterUserDto.builder()
                .username("testUser")
                .email("someEmail")
                .karateClubName("LODZKIE_CENTRUM_OKINAWA_SHORIN_RYU_KARATE_I_KOBUDO")
                .karateRank("INVALID_RANK")
                .role("USER")
                .password("password123")
                .build();

        // when && then
        InvalidUserCredentialsException exception = assertThrows(InvalidUserCredentialsException.class, () -> {
            authService.register(invalidDto);
        });
        assertEquals("Invalid Karate Rank: [INVALID_RANK]", exception.getMessage());
    }

    @Test
    void testRegisterUser_withInvalidRole() {
        // given
        RegisterUserDto invalidDto = RegisterUserDto.builder()
                .username("testUser")
                .email("someEmail")
                .karateClubName("LODZKIE_CENTRUM_OKINAWA_SHORIN_RYU_KARATE_I_KOBUDO")
                .karateRank("KYU_10")
                .role("INVALID_ROLE")
                .password("password123")
                .build();

        // when && then
        InvalidUserCredentialsException exception = assertThrows(InvalidUserCredentialsException.class, () -> {
            authService.register(invalidDto);
        });
        assertEquals("Invalid Role: [INVALID_ROLE]", exception.getMessage());
    }

    @Test
    void testRegisterUser_withNullRole() {
        // given
        RegisterUserDto invalidDto = RegisterUserDto.builder()
                .username("testUser")
                .email("someEmail")
                .karateClubName("LODZKIE_CENTRUM_OKINAWA_SHORIN_RYU_KARATE_I_KOBUDO")
                .karateRank("KYU_10")
                .role(null)
                .password("password123")
                .build();

        // when && then
        InvalidUserCredentialsException exception = assertThrows(InvalidUserCredentialsException.class, () -> {
            authService.register(invalidDto);
        });
        assertEquals("User data cannot be null", exception.getMessage());
    }

    @Test
    void testRegisterUser_withNullPassword() {
        // given
        RegisterUserDto invalidDto = RegisterUserDto.builder()
                .username("testUser")
                .email("someEmail")
                .karateClubName("LODZKIE_CENTRUM_OKINAWA_SHORIN_RYU_KARATE_I_KOBUDO")
                .karateRank("KYU_10")
                .role("USER")
                .password(null)
                .build();

        // when && then
        InvalidUserCredentialsException exception = assertThrows(InvalidUserCredentialsException.class, () -> {
            authService.register(invalidDto);
        });
        assertEquals("User data cannot be null", exception.getMessage());
    }

    @Test
    void testFindByUsername_withExistingUser() {
        // given
        UserEntity userEntity = new UserEntity();
        userEntity.setUserId(1L);
        userEntity.setUsername("testUser");
        userEntity.setEmail("someEmail");
        userEntity.setPassword("password123");
        userEntity.setRoleEntities(Set.of());
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(userEntity));

        // when
        UserDto result = authService.findByUsername("testUser");

        // then
        assertNotNull(result);
        assertEquals("testUser", result.username());
        assertEquals("password123", result.password());
    }
}
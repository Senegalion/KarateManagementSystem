package com.karate.management.karatemanagementsystem.infrastructure.security.jwt;

import com.karate.management.karatemanagementsystem.domain.user.dto.UserDto;
import com.karate.management.karatemanagementsystem.domain.user.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class LoginUserDetailsServiceTest {
    @InjectMocks
    private LoginUserDetailsService loginUserDetailsService;

    @Mock
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoadUserByUsername() {
        // given
        UserDto userDto = new UserDto(1L, "testUser", "password123", Set.of());
        when(authService.findByUsername("testUser")).thenReturn(userDto);

        // when
        var userDetails = loginUserDetailsService.loadUserByUsername("testUser");

        // then
        assertNotNull(userDetails);
        assertEquals("testUser", userDetails.getUsername());
        assertEquals("password123", userDetails.getPassword());
    }
}
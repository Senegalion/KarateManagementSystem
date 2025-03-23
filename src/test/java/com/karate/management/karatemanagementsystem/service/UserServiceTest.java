package com.karate.management.karatemanagementsystem.service;

import com.karate.management.karatemanagementsystem.model.data.KarateRank;
import com.karate.management.karatemanagementsystem.model.dto.UserDetailsDto;
import com.karate.management.karatemanagementsystem.model.entity.AddressEntity;
import com.karate.management.karatemanagementsystem.model.entity.KarateClubEntity;
import com.karate.management.karatemanagementsystem.model.entity.UserEntity;
import com.karate.management.karatemanagementsystem.model.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Optional;

import static com.karate.management.karatemanagementsystem.model.data.KarateClubName.LODZKIE_CENTRUM_OKINAWA_SHORIN_RYU_KARATE_I_KOBUDO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        UserDetails userDetails = User.withUsername("testUser").password("password").roles("USER").build();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void shouldReturnUserDetails_whenUserExists() {
        // given
        UserEntity userEntity = new UserEntity();

        String expectedUsername = "testUser";
        KarateClubEntity expectedKarateClub = new KarateClubEntity(1L, LODZKIE_CENTRUM_OKINAWA_SHORIN_RYU_KARATE_I_KOBUDO, new HashSet<>());
        KarateRank expectedKarateRank = KarateRank.DAN_1;
        AddressEntity expectedAddressEntity = new AddressEntity(1L, "someCity", "someStreet", "1", "12-345", userEntity);

        userEntity.setUsername(expectedUsername);
        userEntity.setKarateClub(expectedKarateClub);
        userEntity.setKarateRank(expectedKarateRank);
        userEntity.setAddressEntity(expectedAddressEntity);

        when(userRepository.getUserByUsername(expectedUsername)).thenReturn(Optional.of(userEntity));

        // when
        UserDetailsDto result = userService.getCurrentUserInfo();

        // then
        assertNotNull(result);
        assertEquals(expectedUsername, result.username());
        assertEquals(expectedKarateClub, result.karateClub());
        assertEquals(expectedKarateRank, result.karateRank());
        assertEquals(expectedAddressEntity, result.addressEntity());
        verify(userRepository, times(1)).getUserByUsername(expectedUsername);
    }

    @Test
    void should_throw_username_not_found_exception_when_user_with_given_username_not_found() {
        // given
        when(userRepository.getUserByUsername("testUser")).thenReturn(Optional.empty());

        // when & then
        assertThrows(UsernameNotFoundException.class, () -> userService.getCurrentUserInfo());
    }
}
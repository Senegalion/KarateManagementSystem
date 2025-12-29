package com.karate.authservice.infrastructure.jwt;

import com.karate.authservice.domain.model.RoleName;
import com.karate.authservice.domain.model.dto.UserDto;
import com.karate.authservice.domain.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoginUserDetailsServiceTest {

    @Test
    void loadUserByUsername_mapsAuthorities() {
        var authService = mock(AuthService.class);
        when(authService.findByUsername("john"))
                .thenReturn(new UserDto(1L, "john", "ENC", Set.of(RoleName.ROLE_USER), "TOKYO"));

        var uds = new com.karate.authservice.infrastructure.jwt.LoginUserDetailsService(authService);
        var ud = uds.loadUserByUsername("john");

        assertThat(ud.getUsername()).isEqualTo("john");
        assertThat(ud.getPassword()).isEqualTo("ENC");
        assertThat(ud.getAuthorities()).extracting(GrantedAuthority::getAuthority).contains("ROLE_USER");
    }
}
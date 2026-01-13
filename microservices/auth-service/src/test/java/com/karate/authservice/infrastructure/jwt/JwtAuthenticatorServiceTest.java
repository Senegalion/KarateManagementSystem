package com.karate.authservice.infrastructure.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.karate.authservice.api.dto.LoginResponseDto;
import com.karate.authservice.api.dto.TokenRequestDto;
import com.karate.authservice.domain.service.AuthService;
import com.karate.authservice.domain.service.UpstreamGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticatorServiceTest {

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    AuthService authService;

    @Mock
    UpstreamGateway upstream;

    @Test
    void authenticateAndGenerateToken_buildsJwt_withClaimsAndDates() {
        // given
        Instant fixed = Instant.parse("2025-01-01T00:00:00Z");
        Clock clock = Clock.fixed(fixed, ZoneOffset.UTC);

        JwtConfigurationProperties props = new JwtConfigurationProperties(
                "super-secret",
                7,
                "my-issuer"
        );

        var svc = new JwtAuthenticatorService(authenticationManager, clock, props, authService, upstream);

        var principal = new User(
                "john",
                "N/A",
                java.util.List.of(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_ADMIN")
                )
        );

        var authResult = mock(org.springframework.security.core.Authentication.class);
        when(authResult.getPrincipal()).thenReturn(principal);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authResult);

        var req = TokenRequestDto.builder()
                .username("john")
                .password("pw")
                .karateClubName("TOKYO")
                .build();

        // when
        LoginResponseDto res = svc.authenticateAndGenerateToken(req);

        // then
        assertThat(res.username()).isEqualTo("john");
        assertThat(res.token()).isNotBlank();

        verify(authenticationManager).authenticate(argThat(t ->
                t.getPrincipal().equals("john") && t.getCredentials().equals("pw")
        ));

        DecodedJWT jwt = JWT.decode(res.token());
        assertThat(jwt.getSubject()).isEqualTo("john");
        assertThat(jwt.getIssuer()).isEqualTo("my-issuer");

        var roles = jwt.getClaim("roles").asList(String.class);
        assertThat(roles).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");

        assertThat(jwt.getIssuedAt().toInstant()).isEqualTo(fixed);
        assertThat(jwt.getExpiresAt().toInstant()).isEqualTo(fixed.plus(Duration.ofDays(7)));
    }
}
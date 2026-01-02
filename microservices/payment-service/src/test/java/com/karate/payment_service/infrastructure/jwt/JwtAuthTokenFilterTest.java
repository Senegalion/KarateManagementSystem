package com.karate.payment_service.infrastructure.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JwtAuthTokenFilterTest {

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_skips_whenNoAuthorizationHeader() throws Exception {
        JwtConfigurationProperties props = new JwtConfigurationProperties("secret", 1, "issuer");
        JwtAuthTokenFilter f = new JwtAuthTokenFilter(props);

        var req = mock(jakarta.servlet.http.HttpServletRequest.class);
        var res = mock(jakarta.servlet.http.HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(req.getHeader(JwtAuthTokenFilter.AUTHORIZATION)).thenReturn(null);

        f.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_setsAuthentication_whenValidBearer() throws Exception {
        JwtConfigurationProperties props = new JwtConfigurationProperties("secret", 1, "issuer");
        JwtAuthTokenFilter f = new JwtAuthTokenFilter(props);

        String token = JWT.create()
                .withSubject("john")
                .withClaim("roles", java.util.List.of("ROLE_USER"))
                .sign(Algorithm.HMAC256("secret"));

        var req = mock(jakarta.servlet.http.HttpServletRequest.class);
        var res = mock(jakarta.servlet.http.HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(req.getHeader(JwtAuthTokenFilter.AUTHORIZATION)).thenReturn("Bearer " + token);

        f.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo("john");
        assertThat(auth.getAuthorities()).extracting("authority").contains("ROLE_USER");
    }
}

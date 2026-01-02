package com.karate.userservice.infrastructure.jwt;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JwtAuthTokenFilterTest {

    @Test
    void doFilterInternal_without_authorization_header_does_not_set_authentication() throws Exception {
        var props = mock(com.karate.userservice.infrastructure.jwt.JwtConfigurationProperties.class);
        when(props.secretKey()).thenReturn("secret");
        var filter = new com.karate.userservice.infrastructure.jwt.JwtAuthTokenFilter(props);

        var req = new org.springframework.mock.web.MockHttpServletRequest();
        var res = new org.springframework.mock.web.MockHttpServletResponse();
        var chain = mock(jakarta.servlet.FilterChain.class);

        org.springframework.security.core.context.SecurityContextHolder.clearContext();

        filter.doFilter(req, res, chain);

        assertThat(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(req, res);
    }

    @Test
    void doFilterInternal_with_non_bearer_header_skips_auth() throws Exception {
        var props = mock(com.karate.userservice.infrastructure.jwt.JwtConfigurationProperties.class);
        when(props.secretKey()).thenReturn("secret");
        var filter = new com.karate.userservice.infrastructure.jwt.JwtAuthTokenFilter(props);

        var req = new org.springframework.mock.web.MockHttpServletRequest();
        req.addHeader("Authorization", "Basic xxx");
        var res = new org.springframework.mock.web.MockHttpServletResponse();
        var chain = mock(jakarta.servlet.FilterChain.class);

        org.springframework.security.core.context.SecurityContextHolder.clearContext();

        filter.doFilter(req, res, chain);

        assertThat(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(req, res);
    }

    @Test
    void doFilterInternal_with_valid_bearer_sets_authentication() throws Exception {
        var props = mock(com.karate.userservice.infrastructure.jwt.JwtConfigurationProperties.class);
        when(props.secretKey()).thenReturn("secret");
        var filter = new com.karate.userservice.infrastructure.jwt.JwtAuthTokenFilter(props);

        String token = com.auth0.jwt.JWT.create()
                .withSubject("john")
                .withClaim("roles", java.util.List.of("ROLE_USER"))
                .sign(com.auth0.jwt.algorithms.Algorithm.HMAC256("secret"));

        var req = new org.springframework.mock.web.MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer " + token);
        var res = new org.springframework.mock.web.MockHttpServletResponse();
        var chain = mock(jakarta.servlet.FilterChain.class);

        org.springframework.security.core.context.SecurityContextHolder.clearContext();

        filter.doFilter(req, res, chain);

        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo("john");
        assertThat(auth.getAuthorities()).extracting(a -> a.getAuthority()).contains("ROLE_USER");
        verify(chain).doFilter(req, res);
    }
}
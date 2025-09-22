package com.karate.userservice.it.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@TestConfiguration
@Profile("test")
public class TestSecurityConfig {

    /**
     * Zastępuje realny JwtAuthTokenFilter w testach.
     * Nagłówki:
     * - X-Test-User: username
     * - X-Test-Roles: CSV ról BEZ 'ROLE_' (np. ADMIN,USER)
     */
    @Bean
    @Primary
    public OncePerRequestFilter testJwtAuthTokenFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(@NotNull HttpServletRequest req,
                                            @NotNull HttpServletResponse res,
                                            @NotNull FilterChain chain)
                    throws ServletException, IOException {
                String user = req.getHeader("X-Test-User");
                String roles = req.getHeader("X-Test-Roles");
                if (user != null) {
                    var authorities = roles == null ? java.util.List.<SimpleGrantedAuthority>of()
                            : Arrays.stream(roles.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isBlank())
                            .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                            .toList();
                    var auth = new UsernamePasswordAuthenticationToken(user, "N/A", authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
                chain.doFilter(req, res);
            }
        };
    }

    @Bean
    @Primary
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   OncePerRequestFilter testJwtAuthTokenFilter) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .anonymous(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(reg -> reg
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/users/by-club").hasRole("ADMIN")
                        .requestMatchers("/internal/**").hasRole("ADMIN")
                        .requestMatchers("/users/me").authenticated()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> res.sendError(401))
                        .accessDeniedHandler((req, res, ex) -> res.sendError(403))
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .addFilterBefore(testJwtAuthTokenFilter, org.springframework.security.web.access.intercept.AuthorizationFilter.class)
                .build();
    }
}

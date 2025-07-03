package com.karate.userservice.infrastructure.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.karate.userservice.api.dto.LoginResponseDto;
import com.karate.userservice.api.dto.TokenRequestDto;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.List;

@Component
@AllArgsConstructor
public class JwtAuthenticatorService {
    private final AuthenticationManager authenticationManager;
    private final Clock clock;
    private final JwtConfigurationProperties jwtConfigurationProperties;

    public LoginResponseDto authenticateAndGenerateToken(TokenRequestDto tokenRequestDto) {
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(tokenRequestDto.username(), tokenRequestDto.password())
        );
        User user = (User) authenticate.getPrincipal();
        String token = createToken(user);
        String username = user.getUsername();
        return LoginResponseDto.builder()
                .username(username)
                .token(token)
                .build();
    }

    private String createToken(User user) {
        String secretKey = jwtConfigurationProperties.secretKey();
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        Instant now = LocalDateTime.now(clock).toInstant(ZoneOffset.UTC);
        Instant expiresAt = now.plus(Duration.ofDays(jwtConfigurationProperties.expirationDays()));
        String issuer = jwtConfigurationProperties.issuer();
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return JWT.create()
                .withSubject(user.getUsername())
                .withClaim("roles", roles)
                .withIssuedAt(now)
                .withExpiresAt(expiresAt)
                .withIssuer(issuer)
                .sign(algorithm);
    }
}

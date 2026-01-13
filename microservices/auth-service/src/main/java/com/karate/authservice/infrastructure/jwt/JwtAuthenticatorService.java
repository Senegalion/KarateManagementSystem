package com.karate.authservice.infrastructure.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.karate.authservice.api.dto.LoginResponseDto;
import com.karate.authservice.api.dto.TokenRequestDto;
import com.karate.authservice.domain.model.RoleName;
import com.karate.authservice.domain.service.AuthService;
import com.karate.authservice.domain.service.UpstreamGateway;
import com.karate.authservice.infrastructure.client.dto.KarateClubDto;
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
    private final AuthService authService;
    private final UpstreamGateway upstream;

    public LoginResponseDto authenticateAndGenerateToken(TokenRequestDto tokenRequestDto) {
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(tokenRequestDto.username(), tokenRequestDto.password())
        );
        User user = (User) authenticate.getPrincipal();
        boolean isSystemAdmin = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(RoleName.ROLE_SYSTEM_ADMIN.name()));
        if (isSystemAdmin) {
            String clubName = tokenRequestDto.karateClubName();
            KarateClubDto club = upstream.getClubByName(clubName);

            Long userId = authService.getUserIdByUsername(user.getUsername());

            upstream.updateUserClubId(userId, club.karateClubId());
        }
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

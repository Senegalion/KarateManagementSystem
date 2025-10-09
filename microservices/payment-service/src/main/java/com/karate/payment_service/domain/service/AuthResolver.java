package com.karate.payment_service.domain.service;

import com.karate.payment_service.infrastructure.client.AuthClient;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthResolver {
    private final AuthClient authClient;

    public Long resolveUserId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }
        String username = auth.getName();
        Long id = authClient.getUserIdByUsername(username);
        if (id == null) {
            throw new IllegalStateException("UserId not found for username=" + username);
        }
        return id;
    }
}

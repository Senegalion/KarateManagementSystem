package com.karate.training_service.domain.service;

import com.karate.training_service.domain.exception.UpstreamUnavailableException;
import com.karate.training_service.infrastructure.client.UserServiceClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpstreamGateway {

    private final UserServiceClient userClient;

    @Cacheable(cacheNames = "userClubIdByUsername_upstream", key = "#username")
    @CircuitBreaker(name = "userService", fallbackMethod = "getUserClubIdFallback")
    @Retry(name = "userService")
    public Long getUserClubId(String username) {
        return userClient.getUserClubId(username);
    }

    private Long getUserClubIdFallback(String username, Throwable ex) {
        log.warn("CB fallback userService.getUserClubId username={} reason={}", username, ex.toString());
        throw new UpstreamUnavailableException("user-service unavailable", ex);
    }
}

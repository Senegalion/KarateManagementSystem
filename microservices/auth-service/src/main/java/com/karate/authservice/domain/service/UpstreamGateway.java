package com.karate.authservice.domain.service;

import com.karate.authservice.domain.exception.UpstreamUnavailableException;
import com.karate.authservice.infrastructure.client.KarateClubClient;
import com.karate.authservice.infrastructure.client.UserClient;
import com.karate.authservice.infrastructure.client.dto.KarateClubDto;
import com.karate.authservice.infrastructure.client.dto.NewUserRequestDto;
import com.karate.authservice.infrastructure.client.dto.UserInfoDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpstreamGateway {
    private final UserClient userClient;
    private final KarateClubClient clubClient;

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByIdFallback")
    @Retry(name = "userService")
    public UserInfoDto getUserById(Long userId) {
        return userClient.getUserById(userId);
    }

    private UserInfoDto getUserByIdFallback(Long userId, Throwable ex) {
        log.warn("CB fallback userService.getUserById userId={} reason={}", userId, ex.toString());
        throw new UpstreamUnavailableException("user-service unavailable", ex);
    }

    @CircuitBreaker(name = "clubService", fallbackMethod = "getClubByIdFallback")
    @Retry(name = "clubService")
    public KarateClubDto getClubById(Long clubId) {
        return clubClient.getClubById(clubId);
    }

    private KarateClubDto getClubByIdFallback(Long clubId, Throwable ex) {
        log.warn("CB fallback clubService.getClubById clubId={} reason={}", clubId, ex.toString());
        throw new UpstreamUnavailableException("club-service unavailable", ex);
    }

    @CircuitBreaker(name = "clubService", fallbackMethod = "getClubByNameFallback")
    @Retry(name = "clubService")
    public KarateClubDto getClubByName(String name) {
        return clubClient.getClubByName(name);
    }

    private KarateClubDto getClubByNameFallback(String name, Throwable ex) {
        log.warn("CB fallback clubService.getClubByName name={} reason={}", name, ex.toString());
        throw new UpstreamUnavailableException("club-service unavailable", ex);
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "createUserAsyncFallback")
    @TimeLimiter(name = "userService")
    public CompletableFuture<Long> createUserAsync(NewUserRequestDto dto) {
        return CompletableFuture.supplyAsync(() -> userClient.createUser(dto));
    }

    private CompletableFuture<Long> createUserAsyncFallback(NewUserRequestDto dto, Throwable ex) {
        log.error("CB/Timeout fallback userService.createUser reason={}", ex.toString());
        return CompletableFuture.failedFuture(new UpstreamUnavailableException("user-service timeout", ex));
    }
}

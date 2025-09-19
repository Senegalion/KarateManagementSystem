package com.karate.userservice.domain.service;

import com.karate.userservice.domain.exception.UpstreamUnavailableException;
import com.karate.userservice.infrastructure.client.AuthClient;
import com.karate.userservice.infrastructure.client.KarateClubClient;
import com.karate.userservice.infrastructure.client.dto.AuthUserDto;
import com.karate.userservice.infrastructure.client.dto.KarateClubDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpstreamGateway {

    private final AuthClient authClient;
    private final KarateClubClient clubClient;

    // --- auth-service ---
    @CircuitBreaker(name = "authService", fallbackMethod = "getAuthUserByUserIdFallback")
    @Retry(name = "authService")
    public AuthUserDto getAuthUserByUserId(Long userId) {
        return authClient.getAuthUserByUserId(userId);
    }

    private AuthUserDto getAuthUserByUserIdFallback(Long userId, Throwable ex) {
        log.warn("CB fallback authService.getAuthUserByUserId userId={} reason={}", userId, ex.toString());
        throw new UpstreamUnavailableException("auth-service unavailable", ex);
    }

    @CircuitBreaker(name = "authService", fallbackMethod = "getAuthUserByUsernameFallback")
    @Retry(name = "authService")
    public AuthUserDto getAuthUserByUsername(String username) {
        return authClient.getAuthUserByUsername(username);
    }

    private AuthUserDto getAuthUserByUsernameFallback(String username, Throwable ex) {
        log.warn("CB fallback authService.getAuthUserByUsername username={} reason={}", username, ex.toString());
        throw new UpstreamUnavailableException("auth-service unavailable", ex);
    }

    @CircuitBreaker(name = "authService", fallbackMethod = "getAuthUsersFallback")
    @Retry(name = "authService")
    public Map<Long, AuthUserDto> getAuthUsers(Iterable<Long> ids) {
        return authClient.getAuthUsers((ids instanceof java.util.List<Long> l) ? l :
                ((java.util.List<Long>) (java.util.stream.StreamSupport.stream(ids.spliterator(), false).toList())));
    }

    private Map<Long, AuthUserDto> getAuthUsersFallback(Iterable<Long> ids, Throwable ex) {
        log.warn("CB fallback authService.getAuthUsers size={} reason={}",
                (ids instanceof java.util.Collection<?> c ? c.size() : -1), ex.toString());
        throw new UpstreamUnavailableException("auth-service unavailable", ex);
    }

    @CircuitBreaker(name = "authService", fallbackMethod = "updateUsernameFallback")
    @TimeLimiter(name = "authService")
    public CompletableFuture<Void> updateUsername(Long userId, String newUsername) {
        return CompletableFuture.runAsync(() -> authClient.updateUsername(userId, newUsername));
    }

    private CompletableFuture<Void> updateUsernameFallback(Long userId, String newUsername, Throwable ex) {
        log.error("CB/Timeout fallback authService.updateUsername userId={} reason={}", userId, ex.toString());
        return CompletableFuture.failedFuture(new UpstreamUnavailableException("auth-service timeout", ex));
    }

    @CircuitBreaker(name = "authService", fallbackMethod = "deleteUserFallback")
    @TimeLimiter(name = "authService")
    public CompletableFuture<Void> deleteUser(Long userId) {
        return CompletableFuture.runAsync(() -> authClient.deleteUser(userId));
    }

    private CompletableFuture<Void> deleteUserFallback(Long userId, Throwable ex) {
        log.error("CB/Timeout fallback authService.deleteUser userId={} reason={}", userId, ex.toString());
        return CompletableFuture.failedFuture(new UpstreamUnavailableException("auth-service timeout", ex));
    }

    // --- club-service ---
    @CircuitBreaker(name = "clubService", fallbackMethod = "getClubByNameFallback")
    @Retry(name = "clubService")
    public KarateClubDto getClubByName(String name) {
        return clubClient.getClubByName(name);
    }

    private KarateClubDto getClubByNameFallback(String name, Throwable ex) {
        log.warn("CB fallback clubService.getClubByName name={} reason={}", name, ex.toString());
        throw new UpstreamUnavailableException("club-service unavailable", ex);
    }

    @CircuitBreaker(name = "clubService", fallbackMethod = "getClubByIdFallback")
    @Retry(name = "clubService")
    public KarateClubDto getClubById(Long id) {
        return clubClient.getClubById(id);
    }

    private KarateClubDto getClubByIdFallback(Long id, Throwable ex) {
        log.warn("CB fallback clubService.getClubById id={} reason={}", id, ex.toString());
        throw new UpstreamUnavailableException("club-service unavailable", ex);
    }
}

package com.karate.enrollment_service.domain.service;

import com.karate.enrollment_service.domain.exception.UpstreamUnavailableException;
import com.karate.enrollment_service.infrastructure.client.AuthClient;
import com.karate.enrollment_service.infrastructure.client.TrainingClient;
import com.karate.enrollment_service.infrastructure.client.UserClient;
import com.karate.enrollment_service.infrastructure.client.dto.TrainingSessionDto;
import com.karate.enrollment_service.infrastructure.client.dto.UserInfoDto;
import com.karate.enrollment_service.infrastructure.client.dto.UserPayload;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpstreamGateway {

    private final UserClient userClient;
    private final AuthClient authClient;
    private final TrainingClient trainingClient;

    @CircuitBreaker(name = "userService", fallbackMethod = "checkUserExistsFallback")
    @Retry(name = "userService")
    public Boolean checkUserExists(Long userId) {
        return userClient.checkUserExists(userId);
    }

    private Boolean checkUserExistsFallback(Long userId, Throwable ex) {
        log.warn("CB fallback userService.checkUserExists userId={} reason={}", userId, ex.toString());
        throw new UpstreamUnavailableException("user-service unavailable", ex);
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserPayloadFallback")
    @Retry(name = "userService")
    public UserPayload getUser(Long userId) {
        return userClient.getUser(userId);
    }

    private UserPayload getUserPayloadFallback(Long userId, Throwable ex) {
        log.warn("CB fallback userService.getUser userId={} reason={}", userId, ex.toString());
        throw new UpstreamUnavailableException("user-service unavailable", ex);
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserInfoFallback")
    @Retry(name = "userService")
    public UserInfoDto getUserById(Long userId) {
        return userClient.getUserById(userId);
    }

    private UserInfoDto getUserInfoFallback(Long userId, Throwable ex) {
        log.warn("CB fallback userService.getUserById userId={} reason={}", userId, ex.toString());
        throw new UpstreamUnavailableException("user-service unavailable", ex);
    }

    // --- auth-service ---
    @CircuitBreaker(name = "userService", fallbackMethod = "getUserIdByUsernameFallback")
    @Retry(name = "userService")
    public Long getUserIdByUsername(String username) {
        return authClient.getUserIdByUsername(username);
    }

    private Long getUserIdByUsernameFallback(String username, Throwable ex) {
        log.warn("CB fallback authService.getUserIdByUsername username={} reason={}", username, ex.toString());
        throw new UpstreamUnavailableException("auth-service unavailable", ex);
    }

    // --- training-service ---
    @CircuitBreaker(name = "trainingService", fallbackMethod = "checkTrainingSessionExistsFallback")
    @Retry(name = "trainingService")
    public Boolean checkTrainingSessionExists(Long trainingId) {
        return trainingClient.checkTrainingSessionExists(trainingId);
    }

    private Boolean checkTrainingSessionExistsFallback(Long trainingId, Throwable ex) {
        log.warn("CB fallback trainingService.checkTrainingSessionExists trainingId={} reason={}", trainingId, ex.toString());
        throw new UpstreamUnavailableException("training-service unavailable", ex);
    }

    @CircuitBreaker(name = "trainingService", fallbackMethod = "getTrainingByIdFallback")
    @Retry(name = "trainingService")
    public TrainingSessionDto getTrainingById(Long trainingId) {
        return trainingClient.getTrainingById(trainingId);
    }

    private TrainingSessionDto getTrainingByIdFallback(Long trainingId, Throwable ex) {
        log.warn("CB fallback trainingService.getTrainingById trainingId={} reason={}", trainingId, ex.toString());
        throw new UpstreamUnavailableException("training-service unavailable", ex);
    }
}

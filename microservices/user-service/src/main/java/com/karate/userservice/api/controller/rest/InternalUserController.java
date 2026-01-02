package com.karate.userservice.api.controller.rest;

import com.karate.userservice.api.dto.NewUserRequestDto;
import com.karate.userservice.api.dto.UserInfoDto;
import com.karate.userservice.api.dto.UserPayload;
import com.karate.userservice.domain.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/internal/users")
@Tag(name = "Internal - Users", description = "Internal API used by other microservices.")
public class InternalUserController {
    private final UserService userService;

    public InternalUserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "Create user (internal)")
    public ResponseEntity<Long> createUser(@RequestBody NewUserRequestDto request) {
        log.info("POST /internal/users userId(auth)={}", request.userId());
        long t0 = System.currentTimeMillis();
        Long userId = userService.createUser(request);
        log.info("200 /internal/users created userId={} took={}ms", userId, System.currentTimeMillis() - t0);
        return ResponseEntity.ok(userId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID (internal)")
    public ResponseEntity<UserInfoDto> getUserById(@PathVariable("id") Long userId) {
        log.info("GET /internal/users/{} ", userId);
        long t0 = System.currentTimeMillis();
        UserInfoDto userInfo = userService.getUserById(userId);
        log.info("200 /internal/users/{} took={}ms", userId, System.currentTimeMillis() - t0);
        return ResponseEntity.ok(userInfo);
    }

    @GetMapping("/{username}/club-id")
    @Operation(summary = "Get user club ID by username (internal)")
    public ResponseEntity<Long> getUserClubId(@PathVariable String username) {
        log.info("GET /internal/users/{}/club-id", username);
        long t0 = System.currentTimeMillis();
        Long clubId = userService.getCurrentUserClubIdByUsername(username);
        log.info("200 /internal/users/{}/club-id clubId={} took={}ms", username, clubId, System.currentTimeMillis() - t0);
        return ResponseEntity.ok(clubId);
    }

    @GetMapping("/{userId}/exists")
    @Operation(summary = "Check if user exists (internal)")
    public ResponseEntity<Boolean> checkUserExists(@PathVariable Long userId) {
        log.info("GET /internal/users/{}/exists", userId);
        boolean exists = userService.checkUserExists(userId);
        log.info("200 /internal/users/{}/exists exists={}", userId, exists);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/payload/{id}")
    @Operation(summary = "Get user payload for token (internal)")
    public ResponseEntity<UserPayload> getUser(@PathVariable("id") Long userId) {
        log.info("GET /internal/users/payload/{}", userId);
        long t0 = System.currentTimeMillis();
        UserPayload userPayload = userService.getUser(userId);
        log.info("200 /internal/users/payload/{} took={}ms", userId, System.currentTimeMillis() - t0);
        return ResponseEntity.ok(userPayload);
    }

    @GetMapping("/{clubId}/users/emails")
    @Operation(summary = "Get emails of users in a club (internal)")
    public ResponseEntity<List<String>> getClubUserEmails(@PathVariable Long clubId) {
        log.info("GET /internal/clubs/{}/users/emails", clubId);
        long t0 = System.currentTimeMillis();
        List<String> emails = userService.getEmailsByClubId(clubId);
        log.info("200 /internal/clubs/{}/users/emails count={} took={}ms",
                clubId, emails.size(), System.currentTimeMillis() - t0);
        return ResponseEntity.ok(emails);
    }
}

package com.karate.userservice.api.controller.rest;

import com.karate.userservice.api.dto.UpdateUserRequestDto;
import com.karate.userservice.api.dto.UserFromClubDto;
import com.karate.userservice.api.dto.UserInformationDto;
import com.karate.userservice.domain.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserRESTController {
    private final UserService userService;

    @Autowired
    public UserRESTController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/by-club")
    public ResponseEntity<List<UserFromClubDto>> getUsersFromClub(@RequestParam("clubName") String clubName) {
        log.info("GET /users/by-club clubName={}", clubName);
        long t0 = System.currentTimeMillis();
        List<UserFromClubDto> users = userService.getUsersFromClubByName(clubName);
        log.info("200 /users/by-club clubName={} size={} took={}ms",
                clubName, users.size(), System.currentTimeMillis() - t0);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/me")
    public ResponseEntity<UserInformationDto> getCurrentUserInfo(Authentication authentication) {
        String username = authentication.getName();
        log.info("GET /users/me user={}", username);
        long t0 = System.currentTimeMillis();
        var body = userService.getCurrentUserInfo(username);
        log.info("200 /users/me user={} took={}ms", username, System.currentTimeMillis() - t0);
        return ResponseEntity.ok(body);
    }

    @PutMapping("/me")
    public ResponseEntity<Void> updateCurrentUser(
            Authentication authentication,
            @Valid @RequestBody UpdateUserRequestDto updateUserRequest
    ) {
        String username = authentication.getName();
        log.info("PUT /users/me user={} newUsername={}", username, updateUserRequest.username());
        long t0 = System.currentTimeMillis();
        userService.updateCurrentUser(username, updateUserRequest);
        log.info("204 /users/me user={} took={}ms", username, System.currentTimeMillis() - t0);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me")
    public ResponseEntity<Void> patchCurrentUser(
            Authentication authentication,
            @RequestBody UpdateUserRequestDto updateUserRequest
    ) {
        String username = authentication.getName();
        log.info("PATCH /users/me user={} payloadProvided={}", username, updateUserRequest != null);
        long t0 = System.currentTimeMillis();
        userService.patchCurrentUser(username, Objects.requireNonNull(updateUserRequest));
        log.info("204 /users/me (patch) user={} took={}ms", username, System.currentTimeMillis() - t0);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        log.info("DELETE /users/me user={}", username);
        long t0 = System.currentTimeMillis();
        userService.deleteCurrentUser(username);
        log.info("204 /users/me (delete) user={} took={}ms", username, System.currentTimeMillis() - t0);
        return ResponseEntity.noContent().build();
    }
}

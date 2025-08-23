package com.karate.userservice.api.controller.rest;

import com.karate.userservice.api.dto.UserFromClubDto;
import com.karate.userservice.api.dto.UserInformationDto;
import com.karate.userservice.domain.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
        List<UserFromClubDto> users = userService.getUsersFromClubByName(clubName);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/me")
    public ResponseEntity<UserInformationDto> getCurrentUserInfo(Authentication authentication) {
        return ResponseEntity.ok(userService.getCurrentUserInfo(authentication.getName()));
    }
}

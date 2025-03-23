package com.karate.management.karatemanagementsystem.controller.rest.user;

import com.karate.management.karatemanagementsystem.model.dto.TrainingSessionDto;
import com.karate.management.karatemanagementsystem.model.dto.UserDetailsDto;
import com.karate.management.karatemanagementsystem.service.TrainingSessionService;
import com.karate.management.karatemanagementsystem.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/users")
public class TrainingSessionUserRESTController {
    private final TrainingSessionService trainingSessionService;
    private final UserService userService;

    @GetMapping("/trainings")
    public ResponseEntity<List<TrainingSessionDto>> getAllTrainingSessions() {
        List<TrainingSessionDto> trainingSessions = trainingSessionService.getAllTrainingSessions();
        return ResponseEntity.ok(trainingSessions);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDetailsDto> getCurrentUserInfo() {
        UserDetailsDto userDetailsDto = userService.getCurrentUserInfo();
        return new ResponseEntity<>(userDetailsDto, HttpStatus.OK);
    }
}

//package com.karate.management.karatemanagementsystem.controller.rest.user;
//
//import com.karate.management.karatemanagementsystem.model.dto.TrainingSessionDto;
//import com.karate.management.karatemanagementsystem.service.TrainingSessionService;
//import lombok.AllArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//
//@AllArgsConstructor
//@RestController
//@RequestMapping("/users")
//public class TrainingSessionUserRESTController {
//    private final TrainingSessionService trainingSessionService;
//
//    @GetMapping("/trainings")
//    public ResponseEntity<List<TrainingSessionDto>> getAllTrainingSessions() {
//        List<TrainingSessionDto> trainingSessions = trainingSessionService.getAllTrainingSessions();
//        return ResponseEntity.ok(trainingSessions);
//    }
//}

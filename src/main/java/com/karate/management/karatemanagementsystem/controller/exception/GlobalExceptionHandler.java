package com.karate.management.karatemanagementsystem.controller.exception;

import com.karate.management.karatemanagementsystem.service.exception.TrainingSessionNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(TrainingSessionNotFoundException.class)
    public ResponseEntity<String> handleEmptyResultDataAccessException(TrainingSessionNotFoundException ex) {
        log.error("No training sessions found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No training sessions found");
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<String> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        log.error("Username does not exist in database: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Username does not exist");
    }

    @ExceptionHandler(UsernameWhileTryingToLogInNotFoundException.class)
    public ResponseEntity<String> handleUsernameNotFoundWhileTryingToLogInException(UsernameWhileTryingToLogInNotFoundException ex) {
        log.error("Username does not exist in database: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Username does not exist");
    }
}

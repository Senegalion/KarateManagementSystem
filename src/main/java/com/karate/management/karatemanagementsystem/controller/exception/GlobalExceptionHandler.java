package com.karate.management.karatemanagementsystem.controller.exception;

import com.karate.management.karatemanagementsystem.service.exception.TrainingSessionNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
}

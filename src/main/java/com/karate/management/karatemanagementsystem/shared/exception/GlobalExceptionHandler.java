package com.karate.management.karatemanagementsystem.shared.exception;

import com.karate.management.karatemanagementsystem.feedback.domain.exception.FeedbackNotFoundException;
import com.karate.management.karatemanagementsystem.domain.payment.PaymentAlreadyConfirmed;
import com.karate.management.karatemanagementsystem.domain.payment.PaymentNotFoundException;
import com.karate.management.karatemanagementsystem.training.domain.exception.TrainingSessionNotFoundException;
import com.karate.management.karatemanagementsystem.user.domain.exception.UserAlreadySignedUpException;
import com.karate.management.karatemanagementsystem.user.domain.exception.UserNotFoundException;
import com.karate.management.karatemanagementsystem.user.domain.exception.UserNotSignedUpException;
import com.karate.management.karatemanagementsystem.user.domain.exception.UsernameWhileTryingToLogInNotFoundException;
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
    public ResponseEntity<String> handleTrainingSessionNotFoundException(TrainingSessionNotFoundException ex) {
        log.error("No training sessions found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
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

    @ExceptionHandler(UserAlreadySignedUpException.class)
    public ResponseEntity<String> handleUserAlreadySignedUpException(UserAlreadySignedUpException ex) {
        log.error("Username has already signed up to this training session: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body("User is already signed up for this session");
    }

    @ExceptionHandler(UserNotSignedUpException.class)
    public ResponseEntity<String> handleUserHasNotBeenSignedUpException(UserNotSignedUpException ex) {
        log.error("Username has not been signed up for this training session: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body("User has not been signed up for this session");
    }

    @ExceptionHandler(FeedbackNotFoundException.class)
    public ResponseEntity<String> handleFeedbackNotFoundException(FeedbackNotFoundException ex) {
        log.error("Feedback not found for this session: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Feedback not found for this session");
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex) {
        log.error("User not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<String> handlePaymentNotFoundException(PaymentNotFoundException ex) {
        log.error("Payment not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(PaymentAlreadyConfirmed.class)
    public ResponseEntity<String> handlePaymentAlreadyConfirmedException(PaymentAlreadyConfirmed ex) {
        log.error("Payment already confirmed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}

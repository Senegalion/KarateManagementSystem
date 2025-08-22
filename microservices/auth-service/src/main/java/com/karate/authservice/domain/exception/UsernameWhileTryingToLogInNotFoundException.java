package com.karate.authservice.domain.exception;

public class UsernameWhileTryingToLogInNotFoundException extends RuntimeException {
    public UsernameWhileTryingToLogInNotFoundException(String message) {
        super(message);
    }
}

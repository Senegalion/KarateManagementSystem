package com.karate.authservice.domain.exception;

public class UsernameWhileTryingToLogInNotFoundException extends Throwable {
    public UsernameWhileTryingToLogInNotFoundException(String message) {
        super(message);
    }
}

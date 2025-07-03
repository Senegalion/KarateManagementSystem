package com.karate.userservice.domain.exception;

public class UsernameWhileTryingToLogInNotFoundException extends Throwable {
    public UsernameWhileTryingToLogInNotFoundException(String message) {
        super(message);
    }
}

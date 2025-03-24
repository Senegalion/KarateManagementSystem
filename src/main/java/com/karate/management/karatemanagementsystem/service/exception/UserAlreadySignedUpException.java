package com.karate.management.karatemanagementsystem.service.exception;

public class UserAlreadySignedUpException extends RuntimeException {
    public UserAlreadySignedUpException(String message) {
        super(message);
    }
}

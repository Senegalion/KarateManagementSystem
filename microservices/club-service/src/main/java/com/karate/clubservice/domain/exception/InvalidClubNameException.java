package com.karate.clubservice.domain.exception;

public class InvalidClubNameException extends RuntimeException {
    public InvalidClubNameException(String message) {
        super(message);
    }
}

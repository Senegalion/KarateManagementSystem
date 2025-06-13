package com.karate.management.karatemanagementsystem.notification.domain.model.exception;

public class EmailSendingException extends RuntimeException {
    public EmailSendingException(String message, Throwable cause) {
        super(message, cause);
    }
}

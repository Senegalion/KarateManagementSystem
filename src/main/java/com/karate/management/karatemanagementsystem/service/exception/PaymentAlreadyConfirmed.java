package com.karate.management.karatemanagementsystem.service.exception;

public class PaymentAlreadyConfirmed extends RuntimeException {
    public PaymentAlreadyConfirmed(String message) {
        super(message);
    }
}

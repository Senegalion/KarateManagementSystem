package com.karate.management.karatemanagementsystem.payment.domain.exception;

public class PaymentAlreadyConfirmed extends RuntimeException {
    public PaymentAlreadyConfirmed(String message) {
        super(message);
    }
}

package com.karate.management.karatemanagementsystem.domain.payment;

public class PaymentAlreadyConfirmed extends RuntimeException {
    public PaymentAlreadyConfirmed(String message) {
        super(message);
    }
}

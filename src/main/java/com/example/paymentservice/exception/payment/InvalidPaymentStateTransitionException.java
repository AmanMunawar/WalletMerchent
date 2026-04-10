package com.example.paymentservice.exception.payment;

public class InvalidPaymentStateTransitionException extends RuntimeException {

    public InvalidPaymentStateTransitionException(String message) {
        super(message);
    }
}
package com.example.paymentservice.exception.payment;

public class RetryNotAllowedException extends RuntimeException {
    public RetryNotAllowedException(String message) {
        super(message);
    }
}
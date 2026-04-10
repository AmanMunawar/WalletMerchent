package com.example.paymentservice.integration;

public interface EventPublisher {

    void publishPaymentResult(PaymentResultEvent event);
}

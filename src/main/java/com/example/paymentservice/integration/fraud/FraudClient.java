package com.example.paymentservice.integration.fraud;


public interface FraudClient {

    FraudCheckResponse evaluateRisk(FraudCheckRequest request);
}
package com.example.paymentservice.integration.fraud;

import org.springframework.stereotype.Component;

@Component
public class StubFraudClient implements FraudClient {

    @Override
    public FraudCheckResponse evaluateRisk(FraudCheckRequest request) {
        FraudCheckResponse response = new FraudCheckResponse();

        response.setRiskDecision("APPROVED");
        response.setRiskScore(0.05d);
        response.setEvaluationReference("frd_" + request.getPaymentId());
        response.setFailureCode(null);
        response.setFailureReason(null);

        return response;
    }
}

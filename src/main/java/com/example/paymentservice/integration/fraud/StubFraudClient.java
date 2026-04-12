package com.example.paymentservice.integration.fraud;

import com.example.paymentservice.properties.StubClientProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StubFraudClient implements FraudClient {

    private final StubClientProperties stubClientProperties;

    @Override
    public FraudCheckResponse evaluateRisk(FraudCheckRequest request) {
        StubClientProperties.Fraud fraudProperties = stubClientProperties.getFraud();
        FraudCheckResponse response = new FraudCheckResponse();
        response.setRiskDecision(fraudProperties.getDecision());
        response.setRiskScore(fraudProperties.getScore().doubleValue());
        response.setEvaluationReference(fraudProperties.getReference());

        if ("APPROVED".equalsIgnoreCase(fraudProperties.getDecision())) {
            response.setFailureCode(null);
            response.setFailureReason(null);
        } else {
            response.setFailureCode(fraudProperties.getFailureCode());
            response.setFailureReason(fraudProperties.getFailureReason());
        }

        return response;
    }
}

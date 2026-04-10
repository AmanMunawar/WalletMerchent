package com.example.paymentservice.integration.fraud;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FraudCheckResponse {

    private String riskDecision;
    private Double riskScore;
    private String evaluationReference;
    private String failureCode;
    private String failureReason;

    public String getRiskDecision() {
        return riskDecision;
    }

    public void setRiskDecision(String riskDecision) {
        this.riskDecision = riskDecision;
    }

    public Double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Double riskScore) {
        this.riskScore = riskScore;
    }

    public String getEvaluationReference() {
        return evaluationReference;
    }

    public void setEvaluationReference(String evaluationReference) {
        this.evaluationReference = evaluationReference;
    }

    public String getFailureCode() {
        return failureCode;
    }

    public void setFailureCode(String failureCode) {
        this.failureCode = failureCode;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}

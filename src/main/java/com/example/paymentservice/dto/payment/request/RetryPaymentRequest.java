package com.example.paymentservice.dto.payment.request;


import jakarta.validation.constraints.NotBlank;

public class RetryPaymentRequest {

    @NotBlank
    private String reason;

    @NotBlank
    private String requestedBy;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }
}
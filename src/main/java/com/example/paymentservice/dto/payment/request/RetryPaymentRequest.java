package com.example.paymentservice.dto.payment.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RetryPaymentRequest {

    @NotBlank(message = "reason is required")
    private String reason;

    @NotBlank(message = "requestedBy is required")
    private String requestedBy;
}
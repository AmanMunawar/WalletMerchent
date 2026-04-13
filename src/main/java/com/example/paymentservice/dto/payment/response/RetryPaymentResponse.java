package com.example.paymentservice.dto.payment.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RetryPaymentResponse {
    private String paymentId;
    private String status;
    private String message;
}

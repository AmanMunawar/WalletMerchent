package com.example.paymentservice.validator.payment;


import com.example.paymentservice.dto.payment.request.CreatePaymentRequest;
import com.example.paymentservice.enums.PaymentMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Set;

@Component
public class PaymentValidator {

    private static final Set<String> SUPPORTED_CURRENCIES = Set.of("USD", "EUR");

    public void validateCreatePaymentRequest(CreatePaymentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body must not be null");
        }

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        if (request.getPaymentMethod() == null || request.getPaymentMethod() != PaymentMethod.WALLET) {
            throw new IllegalArgumentException("Only WALLET payment method is supported");
        }

        if (!StringUtils.hasText(request.getCurrency())) {
            throw new IllegalArgumentException("Currency is required");
        }

        if (!SUPPORTED_CURRENCIES.contains(request.getCurrency().toUpperCase())) {
            throw new IllegalArgumentException("Unsupported currency");
        }

        if (!StringUtils.hasText(request.getIdempotencyKey())) {
            throw new IllegalArgumentException("Idempotency key is required");
        }
    }
}

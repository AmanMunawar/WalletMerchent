package com.example.paymentservice.service.payment;

import com.example.paymentservice.entity.payment.Payment;
import com.example.paymentservice.enums.PaymentStatus;
import com.example.paymentservice.exception.payment.InvalidPaymentStateTransitionException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class PaymentStateManager {

    private static final Map<PaymentStatus, Set<PaymentStatus>> ALLOWED_TRANSITIONS = Map.of(
            PaymentStatus.CREATED, Set.of(PaymentStatus.PROCESSING),
            PaymentStatus.PROCESSING, Set.of(PaymentStatus.SUCCESS, PaymentStatus.FAILED),
            PaymentStatus.FAILED, Set.of(PaymentStatus.PROCESSING),
            PaymentStatus.SUCCESS, Set.of()
    );

    public void validateTransition(PaymentStatus currentStatus, PaymentStatus newStatus) {
        Set<PaymentStatus> allowedNextStates = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of());

        if (!allowedNextStates.contains(newStatus)) {
            throw new InvalidPaymentStateTransitionException(
                    "Invalid payment state transition from " + currentStatus + " to " + newStatus
            );
        }
    }

    public void transition(Payment payment, PaymentStatus newStatus) {
        PaymentStatus currentStatus = payment.getStatus();
        validateTransition(currentStatus, newStatus);
        payment.setStatus(newStatus);
    }

    public boolean isRetryAllowed(Payment payment) {
        if (payment.getStatus() != PaymentStatus.FAILED) {
            return false;
        }

        if (payment.getFailureCode() == null) {
            return false;
        }

        return switch (payment.getFailureCode()) {
            case WALLET_SERVICE_TIMEOUT,
                 LEDGER_WRITE_FAILED,
                 FRAUD_SERVICE_TIMEOUT -> true;

            case FRAUD_REJECTED,
                 INSUFFICIENT_BALANCE,
                 INVALID_REQUEST,
                 RETRY_NOT_ALLOWED -> false;

            default -> false;
        };
    }
}

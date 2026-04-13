package com.example.paymentservice.service.payment;

import com.example.paymentservice.entity.payment.Payment;
import com.example.paymentservice.enums.FailureCode;
import com.example.paymentservice.enums.PaymentStatus;
import com.example.paymentservice.exception.payment.InvalidPaymentStateTransitionException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentStateManagerTest {

    private final PaymentStateManager paymentStateManager = new PaymentStateManager();

    @Test
    void shouldAllowValidTransition() {
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.CREATED);

        paymentStateManager.transition(payment, PaymentStatus.PROCESSING);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PROCESSING);
    }

    @Test
    void shouldRejectInvalidTransition() {
        assertThatThrownBy(() -> paymentStateManager.validateTransition(PaymentStatus.CREATED, PaymentStatus.SUCCESS))
                .isInstanceOf(InvalidPaymentStateTransitionException.class)
                .hasMessage("Invalid payment state transition from CREATED to SUCCESS");
    }

    @Test
    void shouldAllowRetryForRetriableFailureCode() {
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureCode(FailureCode.LEDGER_WRITE_FAILED);

        assertThat(paymentStateManager.isRetryAllowed(payment)).isTrue();
    }

    @Test
    void shouldDenyRetryForNonRetriableFailureCode() {
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureCode(FailureCode.INSUFFICIENT_BALANCE);

        assertThat(paymentStateManager.isRetryAllowed(payment)).isFalse();
    }
}

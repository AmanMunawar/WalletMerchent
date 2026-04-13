package com.example.paymentservice.service.payment;

import com.example.paymentservice.dto.payment.request.RetryPaymentRequest;
import com.example.paymentservice.dto.payment.response.RetryPaymentResponse;
import com.example.paymentservice.entity.payment.Payment;
import com.example.paymentservice.enums.PaymentStatus;
import com.example.paymentservice.exception.payment.PaymentNotFoundException;
import com.example.paymentservice.exception.payment.RetryNotAllowedException;
import com.example.paymentservice.orchestrator.PaymentOrchestrator;
import com.example.paymentservice.repository.payment.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentRetryServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentStateManager paymentStateManager;

    @Mock
    private PaymentOrchestrator paymentOrchestrator;

    @InjectMocks
    private PaymentRetryService paymentRetryService;

    @Test
    void shouldThrowWhenPaymentDoesNotExist() {
        when(paymentRepository.findById("pay_missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentRetryService.retryPayment("pay_missing", retryRequest()))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessage("Payment not found: pay_missing");
    }

    @Test
    void shouldThrowWhenRetryIsNotAllowed() {
        Payment payment = new Payment();
        payment.setPaymentId("pay_001");
        payment.setStatus(PaymentStatus.SUCCESS);

        when(paymentRepository.findById("pay_001")).thenReturn(Optional.of(payment));
        when(paymentStateManager.isRetryAllowed(payment)).thenReturn(false);

        assertThatThrownBy(() -> paymentRetryService.retryPayment("pay_001", retryRequest()))
                .isInstanceOf(RetryNotAllowedException.class)
                .hasMessage("Retry is not allowed for paymentId=pay_001 in status=SUCCESS");
    }

    @Test
    void shouldIncrementRetryCountAndProcessPaymentWhenRetryAllowed() {
        Payment payment = new Payment();
        payment.setPaymentId("pay_001");
        payment.setStatus(PaymentStatus.FAILED);
        payment.setRetryCount(1);

        Payment retriedPayment = new Payment();
        retriedPayment.setPaymentId("pay_001");
        retriedPayment.setStatus(PaymentStatus.SUCCESS);

        when(paymentRepository.findById("pay_001")).thenReturn(Optional.of(payment));
        when(paymentStateManager.isRetryAllowed(payment)).thenReturn(true);
        when(paymentOrchestrator.processPayment("pay_001")).thenReturn(retriedPayment);

        RetryPaymentResponse response = paymentRetryService.retryPayment("pay_001", retryRequest());

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getRetryCount()).isEqualTo(2);
        assertThat(paymentCaptor.getValue().getUpdatedAt()).isNotNull();

        assertThat(response.getPaymentId()).isEqualTo("pay_001");
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getMessage()).isEqualTo("Payment retry initiated successfully");
    }

    private RetryPaymentRequest retryRequest() {
        RetryPaymentRequest request = new RetryPaymentRequest();
        request.setReason("Temporary downstream failure");
        request.setRequestedBy("aman");
        return request;
    }
}

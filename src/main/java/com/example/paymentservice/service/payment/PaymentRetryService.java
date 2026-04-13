package com.example.paymentservice.service.payment;

import com.example.paymentservice.dto.payment.request.RetryPaymentRequest;
import com.example.paymentservice.dto.payment.response.RetryPaymentResponse;
import com.example.paymentservice.entity.payment.Payment;
import com.example.paymentservice.exception.payment.PaymentNotFoundException;
import com.example.paymentservice.exception.payment.RetryNotAllowedException;
import com.example.paymentservice.orchestrator.PaymentOrchestrator;
import com.example.paymentservice.repository.payment.PaymentRepository;
import org.springframework.stereotype.Service;

@Service
public class PaymentRetryService {

    private final PaymentRepository paymentRepository;
    private final PaymentStateManager paymentStateManager;
    private final PaymentOrchestrator paymentOrchestrator;

    public PaymentRetryService(PaymentRepository paymentRepository,
                               PaymentStateManager paymentStateManager,
                               PaymentOrchestrator paymentOrchestrator) {
        this.paymentRepository = paymentRepository;
        this.paymentStateManager = paymentStateManager;
        this.paymentOrchestrator = paymentOrchestrator;
    }

    public RetryPaymentResponse retryPayment(String paymentId, RetryPaymentRequest request) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));

        if (!paymentStateManager.isRetryAllowed(payment)) {
            throw new RetryNotAllowedException(
                    "Retry is not allowed for paymentId=" + paymentId + " in status=" + payment.getStatus()
            );
        }

        Payment retriedPayment = paymentOrchestrator.processPayment(paymentId);

        return RetryPaymentResponse.builder()
                .paymentId(retriedPayment.getPaymentId())
                .status(retriedPayment.getStatus().name())
                .message("Payment retry initiated successfully")
                .build();
    }
}

package com.example.paymentservice.service.payment;

import com.example.paymentservice.dto.payment.request.CreatePaymentRequest;
import com.example.paymentservice.entity.payment.Payment;
import com.example.paymentservice.entity.payment.PaymentStatusHistory;
import com.example.paymentservice.enums.PaymentStatus;
import com.example.paymentservice.orchestrator.PaymentOrchestrator;
import com.example.paymentservice.repository.payment.PaymentRepository;
import com.example.paymentservice.repository.payment.PaymentStatusHistoryRepository;
import com.example.paymentservice.service.idempotency.IdempotencyService;
import com.example.paymentservice.validator.payment.PaymentValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentValidator paymentValidator;
    private final PaymentStatusHistoryRepository paymentStatusHistoryRepository;
    private final IdempotencyService idempotencyService;
    private final PaymentOrchestrator paymentOrchestrator;

    @Transactional
    public PaymentProcessingResult createAndProcessPayment(CreatePaymentRequest request) {
        paymentValidator.validateCreatePaymentRequest(request);

        Optional<Payment> existingPayment = idempotencyService.getExistingPaymentIfSameRequest(request);
        if (existingPayment.isPresent()) {
            return new PaymentProcessingResult(existingPayment.get(), false);
        }

        Payment savedPayment = paymentRepository.save(buildPayment(request));
        paymentStatusHistoryRepository.save(buildCreatedPaymentHistory(savedPayment.getPaymentId()));

        return new PaymentProcessingResult(paymentOrchestrator.processPayment(savedPayment.getPaymentId()), true);
    }

    @Transactional(readOnly = true)
    public Payment getPaymentById(String paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Payment not found"));
    }

    private Payment buildPayment(CreatePaymentRequest request) {
        LocalDateTime now = LocalDateTime.now();

        Payment payment = new Payment();
        payment.setPaymentId("pay_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        payment.setCustomerId(request.getCustomerId());
        payment.setMerchantId(request.getMerchantId());
        payment.setWalletId(request.getWalletId());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus(PaymentStatus.CREATED);
        payment.setTransactionId(null);
        payment.setIdempotencyKey(request.getIdempotencyKey());
        payment.setFailureCode(null);
        payment.setFailureReason(null);
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);
        payment.setRequestHash(idempotencyService.generateRequestHash(request));

        return payment;
    }

    private PaymentStatusHistory buildCreatedPaymentHistory(String paymentId) {
        LocalDateTime now = LocalDateTime.now();

        PaymentStatusHistory paymentStatusHistory = new PaymentStatusHistory();
        paymentStatusHistory.setPaymentId(paymentId);
        paymentStatusHistory.setOldStatus(null);
        paymentStatusHistory.setNewStatus(PaymentStatus.CREATED);
        paymentStatusHistory.setReasonCode("PAYMENT_CREATED");
        paymentStatusHistory.setReasonMessage("Payment created successfully");
        paymentStatusHistory.setChangedAt(now);

        return paymentStatusHistory;
    }

    public record PaymentProcessingResult(Payment payment, boolean created) {
    }
}

package com.example.paymentservice.orchestrator;


import com.example.paymentservice.entity.payment.Payment;
import com.example.paymentservice.enums.FailureCode;
import com.example.paymentservice.enums.FraudCheckStatus;
import com.example.paymentservice.enums.PaymentStatus;
import com.example.paymentservice.integration.PaymentResultEvent;
import com.example.paymentservice.integration.fraud.FraudCheckRequest;
import com.example.paymentservice.integration.fraud.FraudCheckResponse;
import com.example.paymentservice.integration.fraud.FraudClient;
import com.example.paymentservice.integration.ledger.LedgerClient;
import com.example.paymentservice.integration.ledger.LedgerEntryRequest;
import com.example.paymentservice.integration.ledger.LedgerEntryResponse;
import com.example.paymentservice.integration.merchant.MerchantClient;
import com.example.paymentservice.integration.wallet.WalletClient;
import com.example.paymentservice.integration.wallet.WalletDebitRequest;
import com.example.paymentservice.integration.wallet.WalletDebitResponse;
import com.example.paymentservice.repository.payment.PaymentRepository;
import com.example.paymentservice.service.payment.PaymentStateManager;
import com.example.paymentservice.service.payment.PaymentStatusHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentOrchestrator {

    private static final String ACTOR = "PAYMENT_ORCHESTRATOR";

    private final PaymentRepository paymentRepository;
    private final PaymentStateManager paymentStateManager;
    private final PaymentStatusHistoryService paymentStatusHistoryService;

    private final MerchantClient merchantClient;
    private final FraudClient fraudClient;
    private final WalletClient walletClient;
    private final LedgerClient ledgerClient;


    public Payment processPayment(String paymentId) {
        Payment payment = getPayment(paymentId);
        movePaymentToProcessing(payment);
        verifyMerchant(payment);
        performFraudCheck(payment);
        performWalletDebit(payment);
        performLedgerEntry(payment);
        markPaymentSuccessful(payment);
        buildSuccessEvent(payment);
        return payment;
    }

    private Payment getPayment(String paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));
    }

    private void movePaymentToProcessing(Payment payment) {
        PaymentStatus oldStatus = payment.getStatus();
        paymentStateManager.validateTransition(oldStatus, PaymentStatus.PROCESSING);

        payment.setStatus(PaymentStatus.PROCESSING);
        savePayment(payment);

        paymentStatusHistoryService.recordStatusChange(
                payment.getPaymentId(),
                oldStatus,
                PaymentStatus.PROCESSING,
                ACTOR,
                null,
                "Payment processing started"
        );
    }

    private void verifyMerchant(Payment payment) {
        //merchantClient.verifyMerchant(payment.getMerchantId());
    }

    private void performFraudCheck(Payment payment) {
        FraudCheckResponse fraudResponse = fraudClient.evaluateRisk(buildFraudCheckRequest(payment));
        payment.setEvaluationReference(fraudResponse.getEvaluationReference());

        if ("APPROVED".equalsIgnoreCase(fraudResponse.getRiskDecision())) {
            payment.setFraudCheckStatus(FraudCheckStatus.APPROVED);
            savePayment(payment);
            return;
        }

        payment.setFraudCheckStatus(FraudCheckStatus.REJECTED);
        failPayment(payment, fraudResponse.getFailureCode(), fraudResponse.getFailureReason());
        throw new IllegalArgumentException("Payment failed due to fraud rejection");
    }

    private FraudCheckRequest buildFraudCheckRequest(Payment payment) {
        return FraudCheckRequest.builder()
                .paymentId(payment.getPaymentId())
                .customerId(payment.getCustomerId())
                .merchantId(payment.getMerchantId())
                .walletId(payment.getWalletId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod().name())
                .build();
    }

    private void performWalletDebit(Payment payment) {
        WalletDebitResponse walletResponse = walletClient.debit(buildWalletDebitRequest(payment));
        if ("SUCCESS".equalsIgnoreCase(walletResponse.getDebitStatus())) {
            if (payment.getTransactionId() == null || payment.getTransactionId().isBlank()) {
                payment.setTransactionId("txn_" + payment.getPaymentId());
            }
            return;
        }

        failPayment(payment, walletResponse.getFailureCode(), walletResponse.getFailureReason());
        throw new IllegalArgumentException("Payment failed due to wallet debit failure");
    }

    private void performLedgerEntry(Payment payment) {
        LedgerEntryResponse ledgerResponse = ledgerClient.recordEntry(buildLedgerEntryRequest(payment));
        if ("SUCCESS".equalsIgnoreCase(ledgerResponse.getLedgerRecordStatus())) {
            return;
        }

        failPayment(payment, ledgerResponse.getFailureCode(), ledgerResponse.getFailureReason());
        throw new IllegalArgumentException("Payment failed due to ledger write failure");
    }

    private WalletDebitRequest buildWalletDebitRequest(Payment payment) {
        return WalletDebitRequest.builder()
                .walletId(payment.getWalletId())
                .customerId(payment.getCustomerId())
                .paymentId(payment.getPaymentId())
                .transactionReference(payment.getTransactionId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .debitReason("merchant_payment")
                .build();
    }

    private LedgerEntryRequest buildLedgerEntryRequest(Payment payment) {
        return LedgerEntryRequest.builder()
                .paymentId(payment.getPaymentId())
                .transactionId(payment.getTransactionId())
                .customerId(payment.getCustomerId())
                .merchantId(payment.getMerchantId())
                .walletId(payment.getWalletId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .entryType("PAYMENT_DEBIT_CREDIT")
                .postingReference("post_" + payment.getPaymentId())
                .createdTimestamp(LocalDateTime.now())
                .build();
    }

    private void markPaymentSuccessful(Payment payment) {
        PaymentStatus previousStatus = payment.getStatus();
        paymentStateManager.validateTransition(previousStatus, PaymentStatus.SUCCESS);

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setFailureCode(null);
        payment.setFailureReason(null);
        savePayment(payment);

        paymentStatusHistoryService.recordStatusChange(
                payment.getPaymentId(),
                previousStatus,
                PaymentStatus.SUCCESS,
                ACTOR,
                null,
                "Payment completed successfully"
        );
    }

    private void failPayment(Payment payment, String failureCode, String failureReason) {
        payment.setFailureCode(FailureCode.valueOf(failureCode));
        payment.setFailureReason(failureReason);

        PaymentStatus previousStatus = payment.getStatus();
        paymentStateManager.validateTransition(previousStatus, PaymentStatus.FAILED);

        payment.setStatus(PaymentStatus.FAILED);
        savePayment(payment);

        paymentStatusHistoryService.recordStatusChange(
                payment.getPaymentId(),
                previousStatus,
                PaymentStatus.FAILED,
                ACTOR,
                failureCode,
                failureReason
        );
    }

    private PaymentResultEvent buildSuccessEvent(Payment payment) {
        PaymentResultEvent event = PaymentResultEvent.builder()
                .eventType("PAYMENT_SUCCESS")
                .paymentId(payment.getPaymentId())
                .transactionId(payment.getTransactionId())
                .customerId(payment.getCustomerId())
                .merchantId(payment.getMerchantId())
                .status(payment.getStatus().name())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .eventTimestamp(LocalDateTime.now())
                .build();

//        eventPublisher.publish(event);
        savePayment(payment);
        return event;
    }

    private void savePayment(Payment payment) {
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);
    }
}

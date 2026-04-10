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

import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentOrchestrator {

    private final PaymentRepository paymentRepository;
    private final PaymentStateManager paymentStateManager;
    //I am skipping below for now
    private final PaymentStatusHistoryService paymentStatusHistoryService;

    private final MerchantClient merchantClient;
    private final FraudClient fraudClient;
    private final WalletClient walletClient;
    private final LedgerClient ledgerClient;
    //I am skipping below for now
    //private final EventPublisher eventPublisher;

    @Transactional
    public Payment processPayment(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));

        PaymentStatus oldStatus = payment.getStatus();

        paymentStateManager.validateTransition(oldStatus, PaymentStatus.PROCESSING);

        payment.setStatus(PaymentStatus.PROCESSING);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        paymentStatusHistoryService.recordStatusChange(
                payment.getPaymentId(),
                oldStatus,
                PaymentStatus.PROCESSING,
                "PAYMENT_ORCHESTRATOR",
                null,
                "Payment processing started"
        );

        // 1. merchant verify
        //merchantClient.verifyMerchant(payment.getMerchantId());

        // 2. fraud check
        FraudCheckRequest fraudRequest = FraudCheckRequest.builder()
                .paymentId(payment.getPaymentId())
                .customerId(payment.getCustomerId())
                .merchantId(payment.getMerchantId())
                .walletId(payment.getWalletId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod().name())
//                .requestChannel(payment.getRequestChannel())
//                .requestTimestamp(Instant.now())
                .build();

        FraudCheckResponse fraudResponse = fraudClient.evaluateRisk(fraudRequest);

//        payment.setFraudReference(fraudResponse.getEvaluationReference());

        if ("APPROVED".equalsIgnoreCase(fraudResponse.getRiskDecision())) {
//            payment.setFraudCheckStatus(FraudCheckStatus.APPROVED);
        } else {
//            payment.setFraudCheckStatus(FraudCheckStatus.REJECTED);
            payment.setFailureCode(FailureCode.valueOf(fraudResponse.getFailureCode()));
            payment.setFailureReason(fraudResponse.getFailureReason());
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            throw new IllegalStateException("Fraud check not approved");
        }

        // 3. wallet debit
        WalletDebitRequest walletRequest = WalletDebitRequest.builder()
                .walletId(payment.getWalletId())
                .customerId(payment.getCustomerId())
                .paymentId(payment.getPaymentId())
                .transactionReference(payment.getTransactionId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .debitReason("merchant_payment")
//                .requestTimestamp(Instant.now())
                .build();

        WalletDebitResponse walletResponse = walletClient.debit(walletRequest);

        if (!"SUCCESS".equalsIgnoreCase(walletResponse.getDebitStatus())) {
            payment.setFailureCode(FailureCode.valueOf(walletResponse.getFailureCode()));
            payment.setFailureReason(walletResponse.getFailureReason());
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            throw new IllegalStateException("Wallet debit failed");
        }

// wallet success data persist
        if (payment.getTransactionId() == null || payment.getTransactionId().isBlank()) {
            payment.setTransactionId("txn_" + payment.getPaymentId());
        }

        // 4. ledger record
        LedgerEntryRequest ledgerRequest = LedgerEntryRequest.builder()
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

        LedgerEntryResponse ledgerResponse = ledgerClient.recordEntry(ledgerRequest);

        if (!"SUCCESS".equalsIgnoreCase(ledgerResponse.getLedgerRecordStatus())) {
            payment.setFailureCode(FailureCode.valueOf(ledgerResponse.getFailureCode()));
            payment.setFailureReason(ledgerResponse.getFailureReason());
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            throw new IllegalStateException("Ledger write failed");
        }

// 5. move PROCESSING -> SUCCESS
        PaymentStatus previousStatus = payment.getStatus();

        paymentStateManager.validateTransition(previousStatus, PaymentStatus.SUCCESS);

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setFailureCode(null);
        payment.setFailureReason(null);
        payment.setUpdatedAt(LocalDateTime.now());

        paymentRepository.save(payment);

        paymentStatusHistoryService.recordStatusChange(
                payment.getPaymentId(),
                previousStatus,
                PaymentStatus.SUCCESS,
                "PAYMENT_ORCHESTRATOR",
                null,
                "Payment completed successfully"
        );

// 6. publish success event
        PaymentResultEvent event = PaymentResultEvent.builder()
                .eventType("PAYMENT_SUCCESS")
                .paymentId(payment.getPaymentId())
                .transactionId(payment.getTransactionId())
                .customerId(payment.getCustomerId())
                .merchantId(payment.getMerchantId())
//                .walletId(payment.getWalletId())
                .status(payment.getStatus().name())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .eventTimestamp(LocalDateTime.now())
                .build();

//        eventPublisher.publish(event);

        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        return payment;
    }
}

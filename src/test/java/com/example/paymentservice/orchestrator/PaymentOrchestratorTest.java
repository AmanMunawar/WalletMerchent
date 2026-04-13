package com.example.paymentservice.orchestrator;

import com.example.paymentservice.entity.payment.Payment;
import com.example.paymentservice.enums.FailureCode;
import com.example.paymentservice.enums.FraudCheckStatus;
import com.example.paymentservice.enums.PaymentMethod;
import com.example.paymentservice.enums.PaymentStatus;
import com.example.paymentservice.integration.fraud.FraudCheckResponse;
import com.example.paymentservice.integration.fraud.FraudClient;
import com.example.paymentservice.integration.ledger.LedgerClient;
import com.example.paymentservice.integration.ledger.LedgerEntryResponse;
import com.example.paymentservice.integration.merchant.MerchantClient;
import com.example.paymentservice.integration.wallet.WalletClient;
import com.example.paymentservice.integration.wallet.WalletDebitResponse;
import com.example.paymentservice.repository.payment.PaymentRepository;
import com.example.paymentservice.service.payment.PaymentStateManager;
import com.example.paymentservice.service.payment.PaymentStatusHistoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentOrchestratorTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentStateManager paymentStateManager;

    @Mock
    private PaymentStatusHistoryService paymentStatusHistoryService;

    @Mock
    private MerchantClient merchantClient;

    @Mock
    private FraudClient fraudClient;

    @Mock
    private WalletClient walletClient;

    @Mock
    private LedgerClient ledgerClient;

    @InjectMocks
    private PaymentOrchestrator paymentOrchestrator;

    @Test
    void shouldProcessPaymentSuccessfully() {
        Payment payment = basePayment();
        when(paymentRepository.findById("pay_001")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(fraudClient.evaluateRisk(any())).thenReturn(approvedFraudResponse());
        when(walletClient.debit(any())).thenReturn(successWalletResponse());
        when(ledgerClient.recordEntry(any())).thenReturn(successLedgerResponse());

        Payment result = paymentOrchestrator.processPayment("pay_001");

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(result.getFraudCheckStatus()).isEqualTo(FraudCheckStatus.APPROVED);
        assertThat(result.getEvaluationReference()).isEqualTo("frd_ref_001");
        assertThat(result.getTransactionId()).isEqualTo("txn_pay_001");
        verify(paymentStatusHistoryService).recordStatusChange(
                "pay_001",
                PaymentStatus.CREATED,
                PaymentStatus.PROCESSING,
                "PAYMENT_ORCHESTRATOR",
                null,
                "Payment processing started"
        );
        verify(paymentStatusHistoryService).recordStatusChange(
                "pay_001",
                PaymentStatus.PROCESSING,
                PaymentStatus.SUCCESS,
                "PAYMENT_ORCHESTRATOR",
                null,
                "Payment completed successfully"
        );
        verify(paymentRepository, atLeastOnce()).save(payment);
    }

    @Test
    void shouldFailPaymentWhenFraudIsRejected() {
        Payment payment = basePayment();
        when(paymentRepository.findById("pay_001")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(fraudClient.evaluateRisk(any())).thenReturn(rejectedFraudResponse());

        assertThatThrownBy(() -> paymentOrchestrator.processPayment("pay_001"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Payment failed due to fraud rejection");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(payment.getFraudCheckStatus()).isEqualTo(FraudCheckStatus.REJECTED);
        assertThat(payment.getFailureCode()).isEqualTo(FailureCode.FRAUD_REJECTED);
        assertThat(payment.getFailureReason()).isEqualTo("Fraud rejected");
    }

    @Test
    void shouldFailPaymentWhenWalletDebitFails() {
        Payment payment = basePayment();
        when(paymentRepository.findById("pay_001")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(fraudClient.evaluateRisk(any())).thenReturn(approvedFraudResponse());
        when(walletClient.debit(any())).thenReturn(failedWalletResponse());

        assertThatThrownBy(() -> paymentOrchestrator.processPayment("pay_001"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Payment failed due to wallet debit failure");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(payment.getFailureCode()).isEqualTo(FailureCode.INSUFFICIENT_BALANCE);
    }

    @Test
    void shouldFailPaymentWhenLedgerWriteFails() {
        Payment payment = basePayment();
        when(paymentRepository.findById("pay_001")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(fraudClient.evaluateRisk(any())).thenReturn(approvedFraudResponse());
        when(walletClient.debit(any())).thenReturn(successWalletResponse());
        when(ledgerClient.recordEntry(any())).thenReturn(failedLedgerResponse());

        assertThatThrownBy(() -> paymentOrchestrator.processPayment("pay_001"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Payment failed due to ledger write failure");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(payment.getFailureCode()).isEqualTo(FailureCode.LEDGER_WRITE_FAILED);
    }

    private Payment basePayment() {
        Payment payment = new Payment();
        payment.setPaymentId("pay_001");
        payment.setCustomerId("cust_001");
        payment.setMerchantId("merch_001");
        payment.setWalletId("wallet_001");
        payment.setAmount(new BigDecimal("100.00"));
        payment.setCurrency("USD");
        payment.setPaymentMethod(PaymentMethod.WALLET);
        payment.setStatus(PaymentStatus.CREATED);
        payment.setRetryCount(0);
        return payment;
    }

    private FraudCheckResponse approvedFraudResponse() {
        FraudCheckResponse response = new FraudCheckResponse();
        response.setRiskDecision("APPROVED");
        response.setEvaluationReference("frd_ref_001");
        return response;
    }

    private FraudCheckResponse rejectedFraudResponse() {
        FraudCheckResponse response = new FraudCheckResponse();
        response.setRiskDecision("REJECTED");
        response.setEvaluationReference("frd_ref_001");
        response.setFailureCode("FRAUD_REJECTED");
        response.setFailureReason("Fraud rejected");
        return response;
    }

    private WalletDebitResponse successWalletResponse() {
        WalletDebitResponse response = new WalletDebitResponse();
        response.setDebitStatus("SUCCESS");
        return response;
    }

    private WalletDebitResponse failedWalletResponse() {
        WalletDebitResponse response = new WalletDebitResponse();
        response.setDebitStatus("FAILED");
        response.setFailureCode("INSUFFICIENT_BALANCE");
        response.setFailureReason("Insufficient balance");
        return response;
    }

    private LedgerEntryResponse successLedgerResponse() {
        LedgerEntryResponse response = new LedgerEntryResponse();
        response.setLedgerRecordStatus("SUCCESS");
        return response;
    }

    private LedgerEntryResponse failedLedgerResponse() {
        LedgerEntryResponse response = new LedgerEntryResponse();
        response.setLedgerRecordStatus("FAILED");
        response.setFailureCode("LEDGER_WRITE_FAILED");
        response.setFailureReason("Ledger write failed");
        return response;
    }
}

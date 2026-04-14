package com.example.paymentservice.controller.payment;

import com.example.paymentservice.entity.payment.Payment;
import com.example.paymentservice.entity.payment.PaymentStatusHistory;
import com.example.paymentservice.enums.FailureCode;
import com.example.paymentservice.enums.PaymentStatus;
import com.example.paymentservice.properties.StubClientProperties;
import com.example.paymentservice.repository.payment.PaymentRepository;
import com.example.paymentservice.repository.payment.PaymentStatusHistoryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentStatusHistoryRepository paymentStatusHistoryRepository;

    @Autowired
    private StubClientProperties stubClientProperties;

    @BeforeEach
    void setUp() {
        paymentStatusHistoryRepository.deleteAll();
        paymentRepository.deleteAll();

        stubClientProperties.getFraud().setDecision("APPROVED");
        stubClientProperties.getFraud().setFailureCode("FRAUD_REJECTED");
        stubClientProperties.getFraud().setFailureReason("Transaction rejected by stub fraud policy");
        stubClientProperties.getFraud().setReference("fraud_stub_ref");

        stubClientProperties.getWallet().setDebitSuccess(true);
        stubClientProperties.getWallet().setFailureCode("INSUFFICIENT_BALANCE");
        stubClientProperties.getWallet().setFailureReason("Insufficient balance");

        stubClientProperties.getLedger().setRecordSuccess(true);
        stubClientProperties.getLedger().setFailureCode("LEDGER_WRITE_FAILED");
        stubClientProperties.getLedger().setFailureReason("Ledger write failed");
    }

    @Test
    void shouldCreatePaymentAndPersistSuccessfulEndToEndFlow() throws Exception {
        MvcResult result = mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreatePaymentRequest("idem-success")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.transactionId").isNotEmpty())
                .andReturn();

        String paymentId = readJson(result).get("paymentId").asText();

        Payment payment = paymentRepository.findById(paymentId).orElseThrow();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(payment.getFailureCode()).isNull();
        assertThat(payment.getEvaluationReference()).isEqualTo("fraud_stub_ref");
        assertThat(payment.getTransactionId()).isEqualTo("txn_" + paymentId);

        List<PaymentStatusHistory> history = paymentStatusHistoryRepository.findByPaymentIdOrderByChangedAtAsc(paymentId);
        assertThat(history).hasSize(3);
        assertThat(history).extracting(PaymentStatusHistory::getNewStatus)
                .containsExactly(PaymentStatus.CREATED, PaymentStatus.PROCESSING, PaymentStatus.SUCCESS);

        mockMvc.perform(get("/payments/{paymentId}", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(paymentId))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.transactionId").value("txn_" + paymentId));
    }

    @Test
    void shouldReturnExistingPaymentForSameIdempotencyKeyAndPayload() throws Exception {
        MvcResult firstResult = mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreatePaymentRequest("idem-reuse")))
                .andExpect(status().isCreated())
                .andReturn();

        String paymentId = readJson(firstResult).get("paymentId").asText();

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreatePaymentRequest("idem-reuse")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(paymentId))
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        assertThat(paymentRepository.count()).isEqualTo(1);
        assertThat(paymentStatusHistoryRepository.findByPaymentIdOrderByChangedAtAsc(paymentId)).hasSize(3);
    }

    @Test
    void shouldFailPaymentAndAllowRetryAfterLedgerFailure() throws Exception {
        stubClientProperties.getLedger().setRecordSuccess(false);

        MvcResult createResult = mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreatePaymentRequest("idem-retry")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.errorMessage").value("Payment failed due to ledger write failure"))
                .andReturn();

        String failedPaymentId = onlyStoredPaymentId();
        Payment failedPayment = paymentRepository.findById(failedPaymentId).orElseThrow();
        assertThat(failedPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(failedPayment.getFailureCode()).isEqualTo(FailureCode.LEDGER_WRITE_FAILED);
        assertThat(failedPayment.getRetryCount()).isZero();

        stubClientProperties.getLedger().setRecordSuccess(true);

        mockMvc.perform(post("/payments/{paymentId}/retry", failedPaymentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "ledger recovered",
                                  "requestedBy": "integration-test"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(failedPaymentId))
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        Payment retriedPayment = paymentRepository.findById(failedPaymentId).orElseThrow();
        assertThat(retriedPayment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(retriedPayment.getRetryCount()).isEqualTo(1);

        List<PaymentStatusHistory> history = paymentStatusHistoryRepository.findByPaymentIdOrderByChangedAtAsc(failedPaymentId);
        assertThat(history).extracting(PaymentStatusHistory::getNewStatus)
                .containsExactly(
                        PaymentStatus.CREATED,
                        PaymentStatus.PROCESSING,
                        PaymentStatus.FAILED,
                        PaymentStatus.PROCESSING,
                        PaymentStatus.SUCCESS
                );
    }

    @Test
    void shouldRejectRetryForUnknownPayment() throws Exception {
        mockMvc.perform(post("/payments/{paymentId}/retry", "pay_missing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "try again",
                                  "requestedBy": "integration-test"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PAYMENT_NOT_FOUND"))
                .andExpect(jsonPath("$.errorMessage").value("Payment not found: pay_missing"));
    }

    @Test
    void shouldRejectInvalidCreatePaymentRequest() throws Exception {
        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": "cust_001",
                                  "merchantId": "merch_001",
                                  "walletId": "wallet_001",
                                  "amount": 0,
                                  "currency": "USD",
                                  "paymentMethod": "WALLET",
                                  "idempotencyKey": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"));
    }

    @Test
    void shouldExposeHealthEndpoint() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Payment Service is running"));
    }

    private JsonNode readJson(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String onlyStoredPaymentId() {
        return paymentRepository.findAll().stream()
                .map(Payment::getPaymentId)
                .findFirst()
                .orElseThrow();
    }

    private String validCreatePaymentRequest(String idempotencyKey) {
        return """
                {
                  "customerId": "cust_001",
                  "merchantId": "merch_001",
                  "walletId": "wallet_001",
                  "amount": 125.50,
                  "currency": "USD",
                  "paymentMethod": "WALLET",
                  "idempotencyKey": "%s"
                }
                """.formatted(idempotencyKey);
    }
}

package com.example.paymentservice.entity.payment;

import com.example.paymentservice.enums.FailureCode;
import com.example.paymentservice.enums.FraudCheckStatus;
import com.example.paymentservice.enums.PaymentMethod;
import com.example.paymentservice.enums.PaymentStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    public Payment() {
    }

    public Payment(String paymentId, String customerId, String merchantId, String walletId, BigDecimal amount, String currency, PaymentMethod paymentMethod, PaymentStatus status, String transactionId, String idempotencyKey, String requestHash, FailureCode failureCode, String failureReason, LocalDateTime createdAt, LocalDateTime updatedAt, Integer version) {
        this.paymentId = paymentId;
        this.customerId = customerId;
        this.merchantId = merchantId;
        this.walletId = walletId;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.transactionId = transactionId;
        this.idempotencyKey = idempotencyKey;
        this.requestHash = requestHash;
        this.failureCode = failureCode;
        this.failureReason = failureReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    @Id
    @Column(name = "payment_id")
    private String paymentId;

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "merchant_id", nullable = false)
    private String merchantId;

    @Column(name = "wallet_id", nullable = false)
    private String walletId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "fraud_check_status")
    private FraudCheckStatus fraudCheckStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    public String getEvaluationReference() {
        return evaluationReference;
    }

    public void setEvaluationReference(String evaluationReference) {
        this.evaluationReference = evaluationReference;
    }

    @Column(name = "evaluation_reference")
    private String evaluationReference;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "request_hash")
    private String requestHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "failure_code")
    private FailureCode failureCode;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Integer version;

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getWalletId() {
        return walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public void setRequestHash(String requestHash) {
        this.requestHash = requestHash;
    }

    public FailureCode getFailureCode() {
        return failureCode;
    }

    public void setFailureCode(FailureCode failureCode) {
        this.failureCode = failureCode;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public FraudCheckStatus getFraudCheckStatus() {
        return fraudCheckStatus;
    }

    public void setFraudCheckStatus(FraudCheckStatus fraudCheckStatus) {
        this.fraudCheckStatus = fraudCheckStatus;
    }
}

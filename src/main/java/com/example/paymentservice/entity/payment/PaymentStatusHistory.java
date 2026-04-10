package com.example.paymentservice.entity.payment;

import com.example.paymentservice.enums.PaymentStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_status_history")
public class PaymentStatusHistory {

    public PaymentStatusHistory() {
    }

    public PaymentStatusHistory(Long id, String paymentId, PaymentStatus oldStatus, PaymentStatus newStatus, String reasonCode, String reasonMessage, LocalDateTime changedAt) {
        this.id = id;
        this.paymentId = paymentId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.reasonCode = reasonCode;
        this.reasonMessage = reasonMessage;
        this.changedAt = changedAt;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", nullable = false)
    private String paymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status")
    private PaymentStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private PaymentStatus newStatus;

    @Column(name = "reason_code")
    private String reasonCode;

    @Column(name = "reason_message")
    private String reasonMessage;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public PaymentStatus getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(PaymentStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public PaymentStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(PaymentStatus newStatus) {
        this.newStatus = newStatus;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    public String getReasonMessage() {
        return reasonMessage;
    }

    public void setReasonMessage(String reasonMessage) {
        this.reasonMessage = reasonMessage;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}

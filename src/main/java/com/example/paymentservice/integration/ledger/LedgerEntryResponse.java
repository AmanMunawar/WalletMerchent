package com.example.paymentservice.integration.ledger;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class LedgerEntryResponse {

    private String ledgerRecordStatus;
    private String ledgerEntryId;
    private String postingReference;
    private String failureCode;
    private String failureReason;

    public String getLedgerRecordStatus() {
        return ledgerRecordStatus;
    }

    public void setLedgerRecordStatus(String ledgerRecordStatus) {
        this.ledgerRecordStatus = ledgerRecordStatus;
    }

    public String getLedgerEntryId() {
        return ledgerEntryId;
    }

    public void setLedgerEntryId(String ledgerEntryId) {
        this.ledgerEntryId = ledgerEntryId;
    }

    public String getPostingReference() {
        return postingReference;
    }

    public void setPostingReference(String postingReference) {
        this.postingReference = postingReference;
    }

    public String getFailureCode() {
        return failureCode;
    }

    public void setFailureCode(String failureCode) {
        this.failureCode = failureCode;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}

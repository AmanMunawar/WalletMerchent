package com.example.paymentservice.integration.wallet;


import java.math.BigDecimal;

public class WalletDebitResponse {

    private String debitStatus;
    private String walletTransactionId;
    private BigDecimal remainingBalance;
    private String failureCode;
    private String failureReason;

    public String getDebitStatus() {
        return debitStatus;
    }

    public void setDebitStatus(String debitStatus) {
        this.debitStatus = debitStatus;
    }

    public String getWalletTransactionId() {
        return walletTransactionId;
    }

    public void setWalletTransactionId(String walletTransactionId) {
        this.walletTransactionId = walletTransactionId;
    }

    public BigDecimal getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(BigDecimal remainingBalance) {
        this.remainingBalance = remainingBalance;
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
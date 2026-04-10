package com.example.paymentservice.integration.merchant;


public class MerchantVerificationResponse {

    private boolean valid;
    private String merchantStatus;
    private String failureCode;
    private String failureReason;

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getMerchantStatus() {
        return merchantStatus;
    }

    public void setMerchantStatus(String merchantStatus) {
        this.merchantStatus = merchantStatus;
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

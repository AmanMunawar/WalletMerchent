package com.example.paymentservice.integration.merchant;

public interface MerchantClient {

    MerchantVerificationResponse verifyMerchant(MerchantVerificationRequest request);
}

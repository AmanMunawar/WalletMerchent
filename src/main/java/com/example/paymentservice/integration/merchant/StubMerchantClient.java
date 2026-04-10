package com.example.paymentservice.integration.merchant;

import org.springframework.stereotype.Component;

@Component
public class StubMerchantClient implements MerchantClient {

    @Override
    public MerchantVerificationResponse verifyMerchant(MerchantVerificationRequest request) {
        MerchantVerificationResponse response = new MerchantVerificationResponse();

        response.setValid(true);
        response.setMerchantStatus("ACTIVE");
        response.setFailureCode(null);
        response.setFailureReason(null);

        return response;
    }
}

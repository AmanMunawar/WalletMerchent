package com.example.paymentservice.integration.wallet;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class StubWalletClient implements WalletClient {

    @Value("${payment.stub.wallet.mode:SUCCESS}")
    private String mode;
    @Override
    public WalletDebitResponse debit(WalletDebitRequest request) {
        WalletDebitResponse response = new WalletDebitResponse();

        response.setDebitStatus("SUCCESS");
        response.setWalletTransactionId("wtxn_" + request.getPaymentId());
        response.setRemainingBalance(new BigDecimal("1000.00"));
        response.setFailureCode(null);
        response.setFailureReason(null);

        return response;
    }
}
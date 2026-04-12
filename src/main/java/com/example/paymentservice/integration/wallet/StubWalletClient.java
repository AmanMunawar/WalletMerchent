package com.example.paymentservice.integration.wallet;

import com.example.paymentservice.properties.StubClientProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class StubWalletClient implements WalletClient {

    private final StubClientProperties stubClientProperties;

    @Override
    public WalletDebitResponse debit(WalletDebitRequest request) {
        StubClientProperties.Wallet walletProperties = stubClientProperties.getWallet();
        WalletDebitResponse response = new WalletDebitResponse();
        response.setDebitStatus(walletProperties.isDebitSuccess() ? "SUCCESS" : "FAILED");
        response.setWalletTransactionId(walletProperties.getTransactionId());
        response.setRemainingBalance(new BigDecimal(walletProperties.getRemainingBalance()));

        if (walletProperties.isDebitSuccess()) {
            response.setFailureCode(null);
            response.setFailureReason(null);
        } else {
            response.setFailureCode(walletProperties.getFailureCode());
            response.setFailureReason(walletProperties.getFailureReason());
        }

        return response;
    }
}

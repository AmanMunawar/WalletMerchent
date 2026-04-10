package com.example.paymentservice.integration.wallet;

public interface WalletClient {

    WalletDebitResponse debit(WalletDebitRequest request);
}

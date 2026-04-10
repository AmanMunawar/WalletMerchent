package com.example.paymentservice.integration.transaction;

public interface TransactionClient {

    TransactionResponse createTransaction(TransactionRequest request);
}
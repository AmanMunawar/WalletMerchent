package com.example.paymentservice.integration.transaction;

import org.springframework.stereotype.Component;

@Component
public class StubTransactionClient implements TransactionClient {

    @Override
    public TransactionResponse createTransaction(TransactionRequest request) {
        TransactionResponse response = new TransactionResponse();

        response.setTransactionId("txn_" + request.getPaymentId());
        response.setStatus("CREATED");
        response.setFailureCode(null);
        response.setFailureReason(null);

        return response;
    }
}

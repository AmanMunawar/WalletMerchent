package com.example.paymentservice.integration.ledger;

import org.springframework.stereotype.Component;

@Component
public class StubLedgerClient implements LedgerClient {

    @Override
    public LedgerEntryResponse recordEntry(LedgerEntryRequest request) {
        LedgerEntryResponse response = new LedgerEntryResponse();

        response.setLedgerRecordStatus("SUCCESS");
        response.setLedgerEntryId("led_" + request.getPaymentId());
        response.setPostingReference(request.getPostingReference());
        response.setFailureCode(null);
        response.setFailureReason(null);

        return response;
    }
}

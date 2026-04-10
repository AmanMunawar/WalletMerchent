package com.example.paymentservice.integration.ledger;

public interface LedgerClient {

    LedgerEntryResponse recordEntry(LedgerEntryRequest request);
}

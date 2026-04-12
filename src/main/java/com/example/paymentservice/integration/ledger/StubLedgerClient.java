package com.example.paymentservice.integration.ledger;

import com.example.paymentservice.properties.StubClientProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StubLedgerClient implements LedgerClient {

    private final StubClientProperties stubClientProperties;

    @Override
    public LedgerEntryResponse recordEntry(LedgerEntryRequest request) {
        StubClientProperties.Ledger ledgerProperties = stubClientProperties.getLedger();
        LedgerEntryResponse response = new LedgerEntryResponse();
        response.setLedgerRecordStatus(ledgerProperties.isRecordSuccess() ? "SUCCESS" : "FAILED");
        response.setLedgerEntryId(ledgerProperties.getLedgerEntryId());
        response.setPostingReference(ledgerProperties.getPostingReference());

        if (ledgerProperties.isRecordSuccess()) {
            response.setFailureCode(null);
            response.setFailureReason(null);
        } else {
            response.setFailureCode(ledgerProperties.getFailureCode());
            response.setFailureReason(ledgerProperties.getFailureReason());
        }

        return response;
    }
}

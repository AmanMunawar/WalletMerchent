package com.example.paymentservice.properties;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "payment.stub")
public class StubClientProperties {

    private Fraud fraud = new Fraud();
    private Wallet wallet = new Wallet();
    private Ledger ledger = new Ledger();
    private Event event = new Event();
    private Merchant merchant = new Merchant();

    @Getter
    @Setter
    public static class Fraud {
        private String decision = "APPROVED"; // APPROVED / REJECTED
        private Integer score = 10;
        private String reference = "fraud_stub_ref";
        private String failureCode = "FRAUD_REJECTED";
        private String failureReason = "Transaction rejected by stub fraud policy";
    }

    @Getter
    @Setter
    public static class Wallet {
        private boolean debitSuccess = true;
        private String transactionId = "wallet_txn_stub";
        private String remainingBalance = "1000.00";
        private String failureCode = "INSUFFICIENT_BALANCE";
        private String failureReason = "Insufficient balance";
    }

    @Getter
    @Setter
    public static class Ledger {
        private boolean recordSuccess = true;
        private String ledgerEntryId = "ledger_entry_stub";
        private String postingReference = "ledger_post_stub";
        private String failureCode = "LEDGER_WRITE_FAILED";
        private String failureReason = "Ledger write failed";
    }

    @Getter
    @Setter
    public static class Event {
        private boolean publishSuccess = true;
    }

    @Getter
    @Setter
    public static class Merchant {
        private boolean verifySuccess = true;
        private String failureCode = "MERCHANT_NOT_FOUND";
        private String failureReason = "Merchant verification failed";
    }
}

# V1 Scope Gap Analysis (Codebase vs `v1-implementation.md`)

## Overall verdict
V1 scope is **mostly implemented** in the codebase.

## Scope checklist

| V1 scope item | Status | Evidence |
|---|---|---|
| Create Payment API (`POST /payments`) | ✅ Implemented | `PaymentController#createPayment` |
| Get Payment Status API (`GET /payments/{id}`) | ✅ Implemented | `PaymentController#getPaymentStatus` |
| Basic validation | ✅ Implemented | `PaymentValidator`, Bean Validation in request DTO |
| Idempotency handling | ✅ Implemented | `IdempotencyService` + repository lookup/hash comparison |
| Payment persistence using H2 | ✅ Implemented | H2 datasource + JPA + Flyway migration |
| Basic state management (`CREATED -> PROCESSING -> SUCCESS/FAILED`) | ✅ Implemented | `PaymentStateManager` + `PaymentOrchestrator` |
| Mocked downstream interactions | ✅ Implemented (with caveat) | Stub clients exist for Fraud/Wallet/Ledger |

## Items that appear missing/partial

1. **Simulated event publishing via logs is not visibly implemented**
   - `PaymentOrchestrator` builds an event but publisher invocation is commented out.
   - There is no explicit logging call where event simulation is expected.

2. **Merchant downstream step is currently skipped in orchestration**
   - `verifyMerchant(...)` exists but actual call is commented.
   - A `StubMerchantClient` exists in the project, but it is not used in flow.

## Notes about out-of-scope features
- Retry API is already present (`POST /payments/{paymentId}/retry`), which was listed as deferred in V1.
- Additional fraud-state and retry-related behavior exists beyond strict V1 baseline.

## Suggested next updates
- Wire event simulation to logging (or a no-op `EventPublisher` impl) so V1 assumptions are explicitly met.
- Enable merchant verification via stub client (or clarify in V1 doc that merchant check is intentionally skipped).
- Update V1 document status from **In Progress** to **Completed** if the above caveats are accepted.

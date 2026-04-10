# Payment Service - V1 Schema Design

## Purpose
This document defines the database schema for V1 of the Payment Service.
The goal is to have a simple, consistent, and scalable schema that supports payment lifecycle, idempotency, and auditability.

---

## Tables

### 1. payments

#### Purpose
Stores the current state and core metadata of each payment.

#### Fields

- payment_id → Unique identifier for the payment
- customer_id → Customer initiating the payment
- merchant_id → Merchant receiving the payment
- wallet_id → Wallet to be debited

- amount → Payment amount
- currency → Currency of the payment
- payment_method → Payment method (WALLET)

- status → Current payment state (CREATED, PROCESSING, SUCCESS, FAILED)

- transaction_id → Reference to downstream transaction (mock in V1)

- idempotency_key → Prevents duplicate payment requests
- request_hash → Ensures same payload for same idempotency key

- failure_code → Machine-readable failure reason
- failure_reason → Human-readable failure message

- created_at → Payment creation timestamp
- updated_at → Last update timestamp

- version → Used for optimistic locking (future concurrency control)

---

### 2. payment_status_history

#### Purpose
Stores all state transitions for audit and debugging.

#### Fields

- id → Unique identifier for history record
- payment_id → Reference to payment (logical relation)

- old_status → Previous state
- new_status → New state after transition

- reason_code → Machine-readable reason for transition
- reason_message → Human-readable explanation

- changed_at → Timestamp of state change

---

## Relationship

- payment_status_history.payment_id refers to payments.payment_id
- Relationship is maintained logically (no foreign key constraint in V1)

---

## Design Decisions

- Foreign keys are NOT enforced for V1 to keep schema flexible
- Relationship between tables is handled at application level
- Idempotency is implemented using idempotency_key + request_hash
- request_hash ensures same payload for duplicate requests
- version field is added for future optimistic locking support
- payment_retry_attempts table is deferred to later versions

---

## Why These Decisions

- Keep V1 simple and fast to implement
- Avoid unnecessary DB constraints early
- Allow easy schema evolution
- Focus on core payment flow first
- Prepare for future scalability without over-engineering

---

## Summary

This schema supports:
- Payment lifecycle management
- Duplicate request prevention
- Failure tracking
- Audit history of state transitions

This is a minimal but production-aligned schema for V1.

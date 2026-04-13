# Payment Service - V2 Implementation

## Purpose
Defines improvements over V1 with better infrastructure and reliability.

## V2 Scope
- PostgreSQL instead of H2
- Docker setup
- Retry API enabled
- Better idempotency
- Improved error handling
- Detailed retry-attempt audit tracking

## Enhancements
- Persistent DB
- Retry flow
- Better logging
- Add `payment_retry_attempts` table for per-attempt retry traceability
- Persist retry metadata such as `attempt_number`, `triggered_by`, `retry_reason`, `retry_status`, and `created_at`
- Continue using `payments.retry_count` as a quick summary while storing each retry attempt separately for audit and debugging

## Deferred Features
- Real service integrations
- Event-driven architecture

## Goal
- Production-like local setup
- Better retry auditability and operational debugging

## Status
NOT STARTED

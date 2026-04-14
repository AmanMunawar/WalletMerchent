# Payment Service - V2 Implementation

## Purpose
Defines improvements over V1 with better infrastructure, observability, and local run readiness.

## V2 Scope
- PostgreSQL instead of H2
- Docker setup
- Retry API enabled
- Better idempotency
- Improved error handling
- Detailed retry-attempt audit tracking
- Structured logging and request correlation
- Finalized README and local run guide

## Enhancements
- Persistent DB
- Retry flow
- Better logging
- Add `payment_retry_attempts` table for per-attempt retry traceability
- Persist retry metadata such as `attempt_number`, `triggered_by`, `retry_reason`, `retry_status`, and `created_at`
- Continue using `payments.retry_count` as a quick summary while storing each retry attempt separately for audit and debugging
- Add structured logs with fields like `requestId`, `paymentId`, `customerId`, `merchantId`, and `errorCode`
- Add request correlation so a single payment journey can be traced across controller, service, orchestrator, and downstream stub calls
- Improve developer documentation with setup steps, sample APIs, stub modes, retry flow notes, and testing instructions

## Deferred Features
- Real service integrations
- Event-driven architecture

## Goal
- Production-like local setup
- Better retry auditability and operational debugging
- Traceable payment journey through logs
- Easier onboarding for another developer to run the service locally

## Status
NOT STARTED

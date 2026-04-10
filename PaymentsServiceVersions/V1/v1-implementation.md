# Payment Service - V1 Implementation

## Purpose
This document defines the V1 implementation scope for the Payment Service.  
The goal of V1 is to build a runnable, end-to-end working service with minimal dependencies.

## V1 Scope
- Create Payment API (POST /payments)
- Get Payment Status API (GET /payments/{id})
- Basic validation
- Idempotency handling
- Payment persistence using H2 database
- Basic payment state management (CREATED → PROCESSING → SUCCESS/FAILED)
- Mocked downstream service interactions

## Assumptions
- Only WALLET payment method is supported
- Payment Service generates paymentId and transactionId
- Fraud service is not implemented (assumed APPROVED)
- Wallet service is mocked
- Ledger service is mocked
- Event publishing is simulated using logs
- H2 in-memory database is used
- No Docker or external infrastructure required

## Deferred Features
- Retry API
- Fraud REVIEW state handling
- Real Wallet integration
- Real Ledger integration
- Event bus (Kafka/RabbitMQ)
- Postgres database

## Goal of V1
- Runnable local service
- End-to-end API working
- H2 persistence working

## Status
In Progress

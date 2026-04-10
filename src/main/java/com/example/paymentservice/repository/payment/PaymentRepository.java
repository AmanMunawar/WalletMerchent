package com.example.paymentservice.repository.payment;


import com.example.paymentservice.entity.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
}
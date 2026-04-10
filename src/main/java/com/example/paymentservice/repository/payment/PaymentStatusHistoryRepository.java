package com.example.paymentservice.repository.payment;


import com.example.paymentservice.entity.payment.PaymentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentStatusHistoryRepository extends JpaRepository<PaymentStatusHistory, Long> {

    List<PaymentStatusHistory> findByPaymentIdOrderByChangedAtAsc(String paymentId);
}
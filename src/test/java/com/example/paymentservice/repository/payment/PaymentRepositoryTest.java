package com.example.paymentservice.repository.payment;

import com.example.paymentservice.entity.payment.Payment;
import com.example.paymentservice.entity.payment.PaymentStatusHistory;
import com.example.paymentservice.enums.PaymentMethod;
import com.example.paymentservice.enums.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentStatusHistoryRepository paymentStatusHistoryRepository;

    @Test
    void shouldSaveAndFetchPaymentAndHistory() {
        Payment payment = new Payment();
        payment.setPaymentId("pay_001");
        payment.setCustomerId("cust_001");
        payment.setMerchantId("merch_001");
        payment.setWalletId("wall_001");
        payment.setAmount(new BigDecimal("250.00"));
        payment.setCurrency("USD");
        payment.setPaymentMethod(PaymentMethod.WALLET);
        payment.setStatus(PaymentStatus.CREATED);
        payment.setTransactionId(null);
        payment.setIdempotencyKey("idem_001");
        payment.setRequestHash("hash_001");
        payment.setFailureCode(null);
        payment.setFailureReason(null);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
//        payment.setVersion(0);

        paymentRepository.save(payment);

        PaymentStatusHistory history = new PaymentStatusHistory();
        history.setPaymentId("pay_001");
        history.setOldStatus(null);
        history.setNewStatus(PaymentStatus.CREATED);
        history.setReasonCode("PAYMENT_CREATED");
        history.setReasonMessage("Payment created successfully");
        history.setChangedAt(LocalDateTime.now());

        paymentStatusHistoryRepository.save(history);

        Optional<Payment> savedPayment = paymentRepository.findById("pay_001");
        Optional<Payment> savedByIdempotency = paymentRepository.findByIdempotencyKey("idem_001");
        List<PaymentStatusHistory> historyList =
                paymentStatusHistoryRepository.findByPaymentIdOrderByChangedAtAsc("pay_001");

        assertThat(savedPayment).isPresent();
        assertThat(savedPayment.get().getCustomerId()).isEqualTo("cust_001");
        assertThat(savedByIdempotency).isPresent();
        assertThat(historyList).hasSize(1);
        assertThat(historyList.get(0).getNewStatus()).isEqualTo(PaymentStatus.CREATED);
    }
}

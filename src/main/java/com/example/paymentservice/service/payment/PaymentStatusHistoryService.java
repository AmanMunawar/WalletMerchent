package com.example.paymentservice.service.payment;

import com.example.paymentservice.entity.payment.PaymentStatusHistory;
import com.example.paymentservice.enums.PaymentStatus;
import com.example.paymentservice.repository.payment.PaymentStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentStatusHistoryService {

    private final PaymentStatusHistoryRepository paymentStatusHistoryRepository;

    public void recordStatusChange(
            String paymentId,
            PaymentStatus oldStatus,
            PaymentStatus newStatus,
            String changedBy,
            String reasonCode,
            String reasonMessage
    ) {
        PaymentStatusHistory history = new PaymentStatusHistory();
        history.setPaymentId(paymentId);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
//        history.setChangedBy(changedBy);
        history.setReasonCode(reasonCode);
        history.setReasonMessage(reasonMessage);
        history.setChangedAt(LocalDateTime.now());

        paymentStatusHistoryRepository.save(history);
    }
}

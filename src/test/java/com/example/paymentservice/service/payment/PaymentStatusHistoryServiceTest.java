package com.example.paymentservice.service.payment;

import com.example.paymentservice.entity.payment.PaymentStatusHistory;
import com.example.paymentservice.enums.PaymentStatus;
import com.example.paymentservice.repository.payment.PaymentStatusHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentStatusHistoryServiceTest {

    @Mock
    private PaymentStatusHistoryRepository paymentStatusHistoryRepository;

    @InjectMocks
    private PaymentStatusHistoryService paymentStatusHistoryService;

    @Test
    void shouldSaveStatusHistoryRecord() {
        paymentStatusHistoryService.recordStatusChange(
                "pay_001",
                PaymentStatus.CREATED,
                PaymentStatus.PROCESSING,
                "TEST_ACTOR",
                "REASON_CODE",
                "Reason message"
        );

        ArgumentCaptor<PaymentStatusHistory> captor = ArgumentCaptor.forClass(PaymentStatusHistory.class);
        verify(paymentStatusHistoryRepository).save(captor.capture());

        PaymentStatusHistory history = captor.getValue();
        assertThat(history.getPaymentId()).isEqualTo("pay_001");
        assertThat(history.getOldStatus()).isEqualTo(PaymentStatus.CREATED);
        assertThat(history.getNewStatus()).isEqualTo(PaymentStatus.PROCESSING);
        assertThat(history.getReasonCode()).isEqualTo("REASON_CODE");
        assertThat(history.getReasonMessage()).isEqualTo("Reason message");
        assertThat(history.getChangedAt()).isNotNull();
    }
}

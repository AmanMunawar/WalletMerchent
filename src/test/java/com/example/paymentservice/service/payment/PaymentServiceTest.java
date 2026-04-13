package com.example.paymentservice.service.payment;

import com.example.paymentservice.dto.payment.request.CreatePaymentRequest;
import com.example.paymentservice.entity.payment.Payment;
import com.example.paymentservice.entity.payment.PaymentStatusHistory;
import com.example.paymentservice.enums.PaymentMethod;
import com.example.paymentservice.enums.PaymentStatus;
import com.example.paymentservice.orchestrator.PaymentOrchestrator;
import com.example.paymentservice.repository.payment.PaymentRepository;
import com.example.paymentservice.repository.payment.PaymentStatusHistoryRepository;
import com.example.paymentservice.service.idempotency.IdempotencyService;
import com.example.paymentservice.validator.payment.PaymentValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentValidator paymentValidator;

    @Mock
    private PaymentStatusHistoryRepository paymentStatusHistoryRepository;

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private PaymentOrchestrator paymentOrchestrator;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void shouldReturnExistingPaymentWhenSameRequestAlreadyExists() {
        CreatePaymentRequest request = validCreateRequest();
        Payment existingPayment = new Payment();
        existingPayment.setPaymentId("pay_existing");

        when(idempotencyService.getExistingPaymentIfSameRequest(request)).thenReturn(Optional.of(existingPayment));

        PaymentService.PaymentProcessingResult result = paymentService.createAndProcessPayment(request);

        assertThat(result.created()).isFalse();
        assertThat(result.payment()).isSameAs(existingPayment);
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(paymentOrchestrator, never()).processPayment(any(String.class));
    }

    @Test
    void shouldCreatePersistHistoryAndProcessPaymentForNewRequest() {
        CreatePaymentRequest request = validCreateRequest();
        Payment savedPayment = new Payment();
        savedPayment.setPaymentId("pay_001");
        savedPayment.setStatus(PaymentStatus.CREATED);

        Payment processedPayment = new Payment();
        processedPayment.setPaymentId("pay_001");
        processedPayment.setStatus(PaymentStatus.SUCCESS);

        when(idempotencyService.getExistingPaymentIfSameRequest(request)).thenReturn(Optional.empty());
        when(idempotencyService.generateRequestHash(request)).thenReturn("request_hash");
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
        when(paymentOrchestrator.processPayment("pay_001")).thenReturn(processedPayment);

        PaymentService.PaymentProcessingResult result = paymentService.createAndProcessPayment(request);

        assertThat(result.created()).isTrue();
        assertThat(result.payment()).isSameAs(processedPayment);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        Payment capturedPayment = paymentCaptor.getValue();
        assertThat(capturedPayment.getCustomerId()).isEqualTo("cust_001");
        assertThat(capturedPayment.getMerchantId()).isEqualTo("merch_001");
        assertThat(capturedPayment.getWalletId()).isEqualTo("wallet_001");
        assertThat(capturedPayment.getAmount()).isEqualByComparingTo("100.00");
        assertThat(capturedPayment.getCurrency()).isEqualTo("USD");
        assertThat(capturedPayment.getPaymentMethod()).isEqualTo(PaymentMethod.WALLET);
        assertThat(capturedPayment.getStatus()).isEqualTo(PaymentStatus.CREATED);
        assertThat(capturedPayment.getRequestHash()).isEqualTo("request_hash");

        ArgumentCaptor<PaymentStatusHistory> historyCaptor = ArgumentCaptor.forClass(PaymentStatusHistory.class);
        verify(paymentStatusHistoryRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getPaymentId()).isEqualTo("pay_001");
        assertThat(historyCaptor.getValue().getNewStatus()).isEqualTo(PaymentStatus.CREATED);
    }

    @Test
    void shouldThrowWhenPaymentDoesNotExist() {
        when(paymentRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPaymentById("missing"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404 NOT_FOUND");
    }

    private CreatePaymentRequest validCreateRequest() {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setCustomerId("cust_001");
        request.setMerchantId("merch_001");
        request.setWalletId("wallet_001");
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("USD");
        request.setPaymentMethod(PaymentMethod.WALLET);
        request.setIdempotencyKey("idem_001");
        return request;
    }
}

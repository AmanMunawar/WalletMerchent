package com.example.paymentservice.service.idempotency;

import com.example.paymentservice.dto.payment.request.CreatePaymentRequest;
import com.example.paymentservice.entity.payment.Payment;
import com.example.paymentservice.enums.PaymentMethod;
import com.example.paymentservice.exception.payment.IdempotencyConflictException;
import com.example.paymentservice.repository.payment.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private IdempotencyService idempotencyService;

    @Test
    void shouldReturnEmptyWhenNoPaymentExistsForIdempotencyKey() {
        CreatePaymentRequest request = validRequest();
        when(paymentRepository.findByIdempotencyKey("idem_001")).thenReturn(Optional.empty());

        Optional<Payment> result = idempotencyService.getExistingPaymentIfSameRequest(request);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnExistingPaymentWhenRequestHashMatches() {
        CreatePaymentRequest request = validRequest();
        Payment payment = new Payment();
        payment.setRequestHash(idempotencyService.generateRequestHash(request));

        when(paymentRepository.findByIdempotencyKey("idem_001")).thenReturn(Optional.of(payment));

        Optional<Payment> result = idempotencyService.getExistingPaymentIfSameRequest(request);

        assertThat(result).contains(payment);
    }

    @Test
    void shouldThrowWhenSameIdempotencyKeyIsUsedWithDifferentPayload() {
        CreatePaymentRequest request = validRequest();
        Payment payment = new Payment();
        payment.setRequestHash("some_other_hash");

        when(paymentRepository.findByIdempotencyKey("idem_001")).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> idempotencyService.getExistingPaymentIfSameRequest(request))
                .isInstanceOf(IdempotencyConflictException.class)
                .hasMessage("Same idempotency key was used with a different request payload");
    }

    @Test
    void shouldGenerateSameHashForEquivalentNormalizedRequestValues() {
        CreatePaymentRequest request1 = validRequest();
        request1.setCurrency(" usd ");
        request1.setAmount(new BigDecimal("100.00"));

        CreatePaymentRequest request2 = validRequest();
        request2.setCurrency("USD");
        request2.setAmount(new BigDecimal("100"));
        request2.setCustomerId(" cust_001 ");
        request2.setMerchantId(" merch_001 ");
        request2.setWalletId(" wallet_001 ");

        assertThat(idempotencyService.generateRequestHash(request1))
                .isEqualTo(idempotencyService.generateRequestHash(request2));
    }

    private CreatePaymentRequest validRequest() {
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

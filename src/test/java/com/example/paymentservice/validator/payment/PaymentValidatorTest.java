package com.example.paymentservice.validator.payment;

import com.example.paymentservice.dto.payment.request.CreatePaymentRequest;
import com.example.paymentservice.enums.PaymentMethod;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentValidatorTest {

    private final PaymentValidator paymentValidator = new PaymentValidator();

    @Test
    void shouldAcceptValidCreatePaymentRequest() {
        assertThatCode(() -> paymentValidator.validateCreatePaymentRequest(validRequest()))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectNonPositiveAmount() {
        CreatePaymentRequest request = validRequest();
        request.setAmount(BigDecimal.ZERO);

        assertThatThrownBy(() -> paymentValidator.validateCreatePaymentRequest(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Amount must be greater than 0");
    }

    @Test
    void shouldRejectUnsupportedCurrency() {
        CreatePaymentRequest request = validRequest();
        request.setCurrency("PKR");

        assertThatThrownBy(() -> paymentValidator.validateCreatePaymentRequest(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unsupported currency");
    }

    @Test
    void shouldRejectMissingIdempotencyKey() {
        CreatePaymentRequest request = validRequest();
        request.setIdempotencyKey(" ");

        assertThatThrownBy(() -> paymentValidator.validateCreatePaymentRequest(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Idempotency key is required");
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

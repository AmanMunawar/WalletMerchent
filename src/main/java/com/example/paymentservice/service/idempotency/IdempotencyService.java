package com.example.paymentservice.service.idempotency;

import com.example.paymentservice.dto.payment.request.CreatePaymentRequest;
import com.example.paymentservice.entity.payment.Payment;
import com.example.paymentservice.exception.payment.IdempotencyConflictException;
import com.example.paymentservice.repository.payment.PaymentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Service
public class IdempotencyService {

    private final PaymentRepository paymentRepository;

    public IdempotencyService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Optional<Payment> getExistingPaymentIfSameRequest(CreatePaymentRequest request) {
        String requestHash = generateRequestHash(request);

        Optional<Payment> existingPayment =
                paymentRepository.findByIdempotencyKey(request.getIdempotencyKey());

        if (existingPayment.isEmpty()) {
            return Optional.empty();
        }

        Payment payment = existingPayment.get();

        if (requestHash.equals(payment.getRequestHash())) {
            return Optional.of(payment);
        }

        throw new IdempotencyConflictException(
                "Same idempotency key was used with a different request payload"
        );
    }

    public String generateRequestHash(CreatePaymentRequest request) {
        String raw = String.join("|",
                nullSafe(request.getCustomerId()),
                nullSafe(request.getMerchantId()),
                nullSafe(request.getWalletId()),
                normalizeAmount(request.getAmount()),
                normalizeCurrency(request.getCurrency()),
                request.getPaymentMethod() != null ? request.getPaymentMethod().name() : ""
        );

        return sha256(raw);
    }

    private String normalizeAmount(BigDecimal amount) {
        return amount != null ? amount.stripTrailingZeros().toPlainString() : "";
    }

    private String normalizeCurrency(String currency) {
        return currency != null ? currency.trim().toUpperCase() : "";
    }

    private String nullSafe(String value) {
        return value != null ? value.trim() : "";
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hashBytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate request hash", e);
        }
    }
}

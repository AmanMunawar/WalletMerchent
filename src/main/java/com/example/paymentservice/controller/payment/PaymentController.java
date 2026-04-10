package com.example.paymentservice.controller.payment;

import com.example.paymentservice.dto.payment.request.CreatePaymentRequest;
import com.example.paymentservice.dto.payment.response.CreatePaymentResponse;
import com.example.paymentservice.dto.payment.response.GetPaymentStatusResponse;
import com.example.paymentservice.entity.payment.Payment;
import com.example.paymentservice.entity.payment.PaymentStatusHistory;
import com.example.paymentservice.enums.PaymentStatus;
import com.example.paymentservice.orchestrator.PaymentOrchestrator;
import com.example.paymentservice.repository.payment.PaymentRepository;
import com.example.paymentservice.repository.payment.PaymentStatusHistoryRepository;
import com.example.paymentservice.service.idempotency.IdempotencyService;
import com.example.paymentservice.validator.payment.PaymentValidator;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final PaymentValidator paymentValidator;
    private final PaymentStatusHistoryRepository paymentStatusHistoryRepository;
    private final IdempotencyService idempotencyService;
    private final PaymentOrchestrator paymentOrchestrator;

    public PaymentController(PaymentRepository paymentRepository,
                             PaymentValidator paymentValidator,
                             PaymentStatusHistoryRepository paymentStatusHistoryRepository,
                             IdempotencyService idempotencyService,
                             PaymentOrchestrator paymentOrchestrator) {
        this.paymentRepository = paymentRepository;
        this.paymentValidator = paymentValidator;
        this.paymentStatusHistoryRepository = paymentStatusHistoryRepository;
        this.idempotencyService = idempotencyService;
        this.paymentOrchestrator = paymentOrchestrator;
    }

    @PostMapping
    public ResponseEntity<CreatePaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {

        // Validating business requirements
        paymentValidator.validateCreatePaymentRequest(request);

        // Idempotency Check
        Optional<Payment> existingPayment = idempotencyService.getExistingPaymentIfSameRequest(request);

        if (existingPayment.isPresent()) {
            Payment existing = existingPayment.get();
            return ResponseEntity.ok(generateResponseForPayment(existing));
        }

        // saving it into db
        Payment savedPayment = paymentRepository.save(createPaymentForDB(request));

        // initial CREATED history
        PaymentStatusHistory paymentStatusHistory = createPaymentHistory();
        paymentStatusHistory.setPaymentId(savedPayment.getPaymentId());
        paymentStatusHistoryRepository.save(paymentStatusHistory);

        // orchestrator happy path call
        Payment processedPayment = paymentOrchestrator.processPayment(savedPayment.getPaymentId());

        // generating response from final payment state
        CreatePaymentResponse response = generateResponseForPayment(processedPayment);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }


    @GetMapping("/{paymentId}")
    public ResponseEntity<GetPaymentStatusResponse> getPaymentStatus(@PathVariable String paymentId) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));

        GetPaymentStatusResponse response = new GetPaymentStatusResponse();
        response.setPaymentId(payment.getPaymentId());
        response.setTransactionId(payment.getTransactionId());
        response.setStatus(payment.getStatus());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setCustomerId(payment.getCustomerId());
        response.setMerchantId(payment.getMerchantId());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());

        return ResponseEntity.ok(response);
    }

    @NonNull
    private static CreatePaymentResponse generateResponseForPayment(Payment savedPayment) {
        CreatePaymentResponse response = new CreatePaymentResponse();
        response.setPaymentId(savedPayment.getPaymentId());
        response.setTransactionId(savedPayment.getTransactionId());
        response.setStatus(PaymentStatus.CREATED);
        response.setAmount(savedPayment.getAmount());
        response.setCurrency(savedPayment.getCurrency());
        response.setMerchantId(savedPayment.getMerchantId());
        response.setCustomerId(savedPayment.getCustomerId());
        response.setCreatedAt(savedPayment.getCreatedAt());
        return response;
    }

    private Payment createPaymentForDB(CreatePaymentRequest request) {
        LocalDateTime now = LocalDateTime.now();

        Payment payment = new Payment();
        payment.setPaymentId("pay_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        payment.setCustomerId(request.getCustomerId());
        payment.setMerchantId(request.getMerchantId());
        payment.setWalletId(request.getWalletId());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus(PaymentStatus.CREATED);
        payment.setTransactionId(null);
        payment.setIdempotencyKey(request.getIdempotencyKey());
        payment.setFailureCode(null);
        payment.setFailureReason(null);
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);
        payment.setRequestHash(idempotencyService.generateRequestHash(request));
//        payment.setVersion(0);

        return payment;
    }

    private PaymentStatusHistory createPaymentHistory() {
        LocalDateTime now = LocalDateTime.now();
        PaymentStatusHistory paymentStatusHistory = new PaymentStatusHistory();
        paymentStatusHistory.setOldStatus(null);
        paymentStatusHistory.setNewStatus(PaymentStatus.CREATED);
        paymentStatusHistory.setReasonCode("PAYMENT_CREATED");
        paymentStatusHistory.setReasonMessage("Payment created successfully");
        paymentStatusHistory.setChangedAt(now);
        return paymentStatusHistory;
    }
}

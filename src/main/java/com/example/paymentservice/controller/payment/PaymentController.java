package com.example.paymentservice.controller.payment;

import com.example.paymentservice.dto.payment.request.CreatePaymentRequest;
import com.example.paymentservice.dto.payment.response.CreatePaymentResponse;
import com.example.paymentservice.dto.payment.response.GetPaymentStatusResponse;
import com.example.paymentservice.entity.payment.Payment;
import com.example.paymentservice.service.payment.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<CreatePaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        PaymentService.PaymentProcessingResult result = paymentService.createAndProcessPayment(request);
        if (result.created()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(generateCreatePaymentResponse(result.payment()));
        }
        return ResponseEntity.ok(generateCreatePaymentResponse(result.payment()));
    }


    @GetMapping("/{paymentId}")
    public ResponseEntity<GetPaymentStatusResponse> getPaymentStatus(@PathVariable String paymentId) {
        return ResponseEntity.ok(generateGetPaymentStatusResponse(paymentService.getPaymentById(paymentId)));
    }

    private CreatePaymentResponse generateCreatePaymentResponse(Payment payment) {
        CreatePaymentResponse response = new CreatePaymentResponse();
        response.setPaymentId(payment.getPaymentId());
        response.setTransactionId(payment.getTransactionId());
        response.setStatus(com.example.paymentservice.enums.PaymentStatus.CREATED);
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setMerchantId(payment.getMerchantId());
        response.setCustomerId(payment.getCustomerId());
        response.setCreatedAt(payment.getCreatedAt());
        return response;
    }

    private GetPaymentStatusResponse generateGetPaymentStatusResponse(Payment payment) {
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
        return response;
    }
}

//package com.example.paymentservice.service.payment;
//
//import com.example.paymentservice.dto.payment.request.CreatePaymentRequest;
//import com.example.paymentservice.entity.payment.Payment;
//import com.example.paymentservice.entity.payment.PaymentStatusHistory;
//import org.springframework.stereotype.Service;
//
//import java.util.Optional;
//
//@Service
//public class PaymentService {
//
//    public Payment createAndProcessPayment(CreatePaymentRequest request) {
//        paymentValidator.validateCreatePaymentRequest(request);
//
//        Optional<Payment> existingPayment = idempotencyService.getExistingPaymentIfSameRequest(request);
//        if (existingPayment.isPresent()) {
//            return existingPayment.get();
//        }
//
//        Payment savedPayment = paymentRepository.save(createPaymentForDB(request));
//
//        PaymentStatusHistory history = createPaymentHistory();
//        history.setPaymentId(savedPayment.getPaymentId());
//        paymentStatusHistoryRepository.save(history);
//
//        return paymentOrchestrator.processPayment(savedPayment.getPaymentId());
//    }
//}


package com.eigen.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eigen.payment.dto.PaymentRequest;
import com.eigen.payment.dto.PaymentResponse;
import com.eigen.payment.entity.Payment;
import com.eigen.payment.event.PaymentEventPublisher;
import com.eigen.payment.repository.PaymentRepository;
import com.eigen.payment.security.EncryptionService;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final EncryptionService encryptionService;
    private final PaymentEventPublisher eventPublisher;

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for merchant: {}", request.getMerchantId());

        try {
            // Check for existing transaction (idempotency)
            if (request.getTransactionId() != null) {
                Payment existingPayment = paymentRepository.findByTransactionReference(request.getTransactionId()).orElse(null);
                if (existingPayment != null) {
                    log.info("Duplicate transaction detected: {}", request.getTransactionId());
                    return buildPaymentResponse(existingPayment);
                }
            }

            // Encrypt sensitive data
            String encryptedCardNumber = encryptionService.encrypt(request.getCardNumber());

            // Create payment entity
            Payment payment = Payment.builder()
                    .cardNumberEncrypted(encryptedCardNumber)
                    .cardHolderName(request.getCardHolderName())
                    .expiryMonth(String.valueOf(request.getExpiryMonth()))
                    .expiryYear(String.valueOf(request.getExpiryYear()))
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .description(request.getDescription())
                    .merchantId(request.getMerchantId())
                    .status(Payment.PaymentStatus.INITIATED)
                    .transactionReference(request.getTransactionId() != null ? request.getTransactionId()
                            : UUID.randomUUID().toString())
                    .build();

            // Save payment
            payment = paymentRepository.save(payment);

            // Publish PAYMENT_INITIATED event to Kafka
            eventPublisher.publishPaymentInitiated(payment);

            // Update status to processing
            payment.setStatus(Payment.PaymentStatus.PROCESSING);
            payment = paymentRepository.save(payment);

            // Simulate payment processing
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            payment = paymentRepository.save(payment);

            // Publish PAYMENT_COMPLETED event and settlement message
            eventPublisher.publishPaymentCompleted(payment);
            eventPublisher.publishSettlementRequest(payment);

            log.info("Payment processed successfully: {}", payment.getId());

            return buildPaymentResponse(payment);

        } catch (Exception e) {
            log.error("Payment processing failed: {}", e.getMessage(), e);

            return PaymentResponse.builder()
                    .status("FAILED")
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    private PaymentResponse buildPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .status(payment.getStatus().name())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .description(payment.getDescription())
                .merchantId(payment.getMerchantId())
                .processedAt(payment.getCreatedAt())
                .transactionReference(payment.getTransactionReference())
                .build();
    }

    public PaymentResponse getPayment(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .status(payment.getStatus().name())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .description(payment.getDescription())
                .merchantId(payment.getMerchantId())
                .processedAt(payment.getCreatedAt())
                .transactionReference(payment.getTransactionReference())
                .build();
    }

    public List<PaymentResponse> getPaymentHistory() {
        log.info("Retrieving payment history");
        try {
            List<Payment> payments = paymentRepository.findAllByOrderByCreatedAtDesc();

            if (payments.isEmpty()) {
                log.info("No payment records found");
                return Collections.emptyList();
            }

            log.info("Found {} payment records", payments.size());
            return payments.stream()
                    .map(this::buildPaymentResponse)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error retrieving payment history: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve payment history", e);
        }
    }
}

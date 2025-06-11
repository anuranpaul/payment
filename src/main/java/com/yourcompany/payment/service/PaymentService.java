
package com.yourcompany.payment.service;

import com.yourcompany.payment.dto.PaymentRequest;
import com.yourcompany.payment.dto.PaymentResponse;
import com.yourcompany.payment.entity.Payment;
import com.yourcompany.payment.event.PaymentEventPublisher;
import com.yourcompany.payment.repository.PaymentRepository;
import com.yourcompany.payment.security.EncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

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
            // Encrypt sensitive data
            String encryptedCardNumber = encryptionService.encrypt(request.getCardNumber());
            
            // Create payment entity
            Payment payment = Payment.builder()
                    .cardNumberEncrypted(encryptedCardNumber)
                    .cardHolderName(request.getCardHolderName())
                    .expiryMonth(request.getExpiryMonth())
                    .expiryYear(request.getExpiryYear())
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .description(request.getDescription())
                    .merchantId(request.getMerchantId())
                    .status(Payment.PaymentStatus.PROCESSING)
                    .transactionReference(UUID.randomUUID().toString())
                    .build();
            
            // Save payment
            payment = paymentRepository.save(payment);
            
            // Simulate payment processing
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            payment = paymentRepository.save(payment);
            
            // Publish event
            eventPublisher.publishPaymentProcessed(payment);
            
            log.info("Payment processed successfully: {}", payment.getId());
            
            return PaymentResponse.builder()
                    .paymentId(payment.getId())
                    .status(payment.getStatus().name())
                    .amount(payment.getAmount())
                    .currency(payment.getCurrency())
                    .description(payment.getDescription())
                    .merchantId(payment.getMerchantId())
                    .processedAt(LocalDateTime.now())
                    .transactionReference(payment.getTransactionReference())
                    .build();
                    
        } catch (Exception e) {
            log.error("Payment processing failed: {}", e.getMessage(), e);
            
            return PaymentResponse.builder()
                    .status("FAILED")
                    .errorMessage(e.getMessage())
                    .build();
        }
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
}

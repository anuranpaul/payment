package com.eigen.payment.service;

import com.eigen.payment.entity.Payment;
import com.eigen.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final PaymentRepository paymentRepository;

    @KafkaListener(topics = "payment-events", groupId = "payment-processor")
    public void handlePaymentEvent(
            @Payload String message,
            @Header(name = "kafka_receivedMessageKey") String transactionId,
            Acknowledgment acknowledgment) {

        try {
            log.info("Received payment event: {} for transaction: {}", message, transactionId);

            // Idempotent processing - check if we've already processed this transaction
            Payment payment = paymentRepository.findByTransactionReference(transactionId).orElse(null);
            if (payment == null) {
                log.warn("Payment not found for transaction: {}", transactionId);
                acknowledgment.acknowledge();
                return;
            }

            // Process the event based on type
            if (message.contains("PAYMENT_INITIATED")) {
                log.info("Processing payment initiation for: {}", payment.getId());
                // Additional processing logic here
            } else if (message.contains("PAYMENT_COMPLETED")) {
                log.info("Processing payment completion for: {}", payment.getId());
                // Additional processing logic here
            }

            // Acknowledge successful processing
            acknowledgment.acknowledge();
            log.info("Successfully processed payment event for: {}", transactionId);

        } catch (Exception e) {
            log.error("Error processing payment event: {}", e.getMessage(), e);
            // Don't acknowledge - message will be retried
        }
    }
}
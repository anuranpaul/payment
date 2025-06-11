
package com.eigen.payment.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.eigen.payment.config.RabbitConfig.*;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.eigen.payment.entity.Payment;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final RabbitTemplate rabbitTemplate;

    public void publishPaymentProcessed(Payment payment) {
        String message = String.format("Payment processed: %s, Amount: %s %s", 
                payment.getId(), payment.getAmount(), payment.getCurrency());
        
        // Publish to Kafka
        try {
            kafkaTemplate.send("payment-events", payment.getId(), message);
            log.info("Published payment event to Kafka: {}", payment.getId());
        } catch (Exception e) {
            log.error("Failed to publish to Kafka: {}", e.getMessage());
        }
        
        // Publish to RabbitMQ
        try {
            PaymentEvent event = PaymentEvent.builder()
                    .paymentId(payment.getId())
                    .status(payment.getStatus().name())
                    .amount(payment.getAmount())
                    .currency(payment.getCurrency())
                    .merchantId(payment.getMerchantId())
                    .transactionReference(payment.getTransactionReference())
                    .build();
                    
            rabbitTemplate.convertAndSend(PAYMENT_EXCHANGE, PAYMENT_ROUTING_KEY, event);
            log.info("Published payment event to RabbitMQ: {}", payment.getId());
        } catch (Exception e) {
            log.error("Failed to publish to RabbitMQ: {}", e.getMessage());
        }
    }
}


package com.eigen.payment.event;

import com.eigen.payment.entity.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.eigen.payment.config.RabbitConfig.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final RabbitTemplate rabbitTemplate;

    public void publishPaymentInitiated(Payment payment) {
        String eventType = "PAYMENT_INITIATED";
        publishToKafka(payment, eventType);
        log.info("Published {} event to Kafka: {}", eventType, payment.getId());
    }

    public void publishPaymentCompleted(Payment payment) {
        String eventType = "PAYMENT_COMPLETED";
        publishToKafka(payment, eventType);
        log.info("Published {} event to Kafka: {}", eventType, payment.getId());
    }

    public void publishSettlementRequest(Payment payment) {
        try {
            SettlementEvent settlementEvent = SettlementEvent.builder()
                    .paymentId(payment.getId())
                    .merchantId(payment.getMerchantId())
                    .amount(payment.getAmount())
                    .currency(payment.getCurrency())
                    .transactionReference(payment.getTransactionReference())
                    .status("PENDING")
                    .timestamp(LocalDateTime.now())
                    .build();
                    
            rabbitTemplate.convertAndSend(SETTLEMENT_EXCHANGE, SETTLEMENT_ROUTING_KEY, settlementEvent);
            log.info("Published settlement request to RabbitMQ: {}", payment.getId());
        } catch (Exception e) {
            log.error("Failed to publish settlement request to RabbitMQ: {}", e.getMessage());
        }
    }

    private void publishToKafka(Payment payment, String eventType) {
        try {
            PaymentEvent event = PaymentEvent.builder()
                    .paymentId(payment.getId())
                    .eventType(eventType)
                    .status(payment.getStatus().name())
                    .amount(payment.getAmount())
                    .currency(payment.getCurrency())
                    .merchantId(payment.getMerchantId())
                    .transactionReference(payment.getTransactionReference())
                    .timestamp(LocalDateTime.now())
                    .build();
                    
            kafkaTemplate.send("payment-events", payment.getTransactionReference(), event.toString());
        } catch (Exception e) {
            log.error("Failed to publish {} event to Kafka: {}", eventType, e.getMessage());
        }
    }
}
package com.eigen.payment.service;


import com.eigen.payment.entity.Payment;
import com.eigen.payment.repository.PaymentRepository;
import com.eigen.payment.event.SettlementEvent; 
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.eigen.payment.config.RabbitConfig.SETTLEMENT_QUEUE;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementWorker {

    private final PaymentRepository paymentRepository;

    @RabbitListener(queues = SETTLEMENT_QUEUE)
    @Transactional
    public void processSettlement(SettlementEvent settlementEvent) {
        try {
            log.info("Processing settlement for payment: {}", settlementEvent.getPaymentId());

            Payment payment = paymentRepository.findById(settlementEvent.getPaymentId())
                    .orElseThrow(() -> new RuntimeException("Payment not found: " + settlementEvent.getPaymentId()));

            // Update payment status to settlement pending
            payment.setStatus(Payment.PaymentStatus.SETTLEMENT_PENDING);
            paymentRepository.save(payment);

            // Simulate settlement processing
            Thread.sleep(1000); // Simulate external settlement API call

            //random failure simulation
            if (Math.random() < 0.2) { // 10% chance of failure
                throw new RuntimeException("Simulated settlement failure");
            }

            // Update payment status to settled
            payment.setStatus(Payment.PaymentStatus.SETTLED);
            paymentRepository.save(payment);

            log.info("Settlement completed for payment: {}", payment.getId());

        } catch (Exception e) {
            log.error("Settlement processing failed for payment: {}", settlementEvent.getPaymentId(), e);

            // Update payment status to failed
            Payment payment = paymentRepository.findById(settlementEvent.getPaymentId()).orElse(null);
            if (payment != null) {
                payment.setStatus(Payment.PaymentStatus.FAILED);
                paymentRepository.save(payment);
            }

            throw new RuntimeException("Settlement failed", e);
        }
    }
}
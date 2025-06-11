package com.eigen.payment.event;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SettlementEvent {
    private String paymentId;
    private String merchantId;
    private BigDecimal amount;
    private String currency;
    private String transactionReference;
    private String status; // PENDING, COMPLETED, FAILED
    private LocalDateTime timestamp;
}

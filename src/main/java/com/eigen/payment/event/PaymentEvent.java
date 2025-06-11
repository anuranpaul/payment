
package com.eigen.payment.event;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentEvent {
    private String paymentId;
    private String status;
    private BigDecimal amount;
    private String currency;
    private String merchantId;
    private String transactionReference;
    private LocalDateTime timestamp;
}

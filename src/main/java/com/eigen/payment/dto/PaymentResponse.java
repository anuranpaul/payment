
package com.eigen.payment.dto;

import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    
    private String paymentId;
    private String status;
    private BigDecimal amount;
    private String currency;
    private String description;
    private String merchantId;
    private LocalDateTime processedAt;
    private String transactionReference;
    private String errorMessage;
}

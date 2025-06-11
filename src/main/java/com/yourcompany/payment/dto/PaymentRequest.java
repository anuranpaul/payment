
package com.yourcompany.payment.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
public class PaymentRequest {
    
    @NotBlank(message = "Card number is required")
    private String cardNumber;
    
    @NotBlank(message = "Card holder name is required")
    private String cardHolderName;
    
    @NotBlank(message = "Expiry month is required")
    private String expiryMonth;
    
    @NotBlank(message = "Expiry year is required")
    private String expiryYear;
    
    @NotBlank(message = "CVV is required")
    private String cvv;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    private String currency;
    
    private String description;
    
    @NotBlank(message = "Merchant ID is required")
    private String merchantId;
}

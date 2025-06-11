
package com.yourcompany.payment.controller;

import com.yourcompany.payment.dto.PaymentRequest;
import com.yourcompany.payment.dto.PaymentResponse;
import com.yourcompany.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable String paymentId) {
        PaymentResponse response = paymentService.getPayment(paymentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Payment service is healthy!");
    }
}


package com.eigen.payment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import com.eigen.payment.dto.PaymentRequest;
import com.eigen.payment.dto.PaymentResponse;
import com.eigen.payment.service.PaymentService;

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
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable("paymentId") String paymentId) {
        PaymentResponse response = paymentService.getPayment(paymentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>> getPaymentHistory() {
        List<PaymentResponse> history = paymentService.getPaymentHistory();
        return ResponseEntity.ok(history);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Payment service is healthy!");
    }
}

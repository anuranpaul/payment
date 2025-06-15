package com.eigen.payment.repository;

import com.eigen.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    Optional<Payment> findByTransactionReference(String transactionReference);

    List<Payment> findByMerchantIdAndStatus(String merchantId, Payment.PaymentStatus status);

    List<Payment> findAllByOrderByCreatedAtDesc();

    @Query("SELECT p FROM Payment p WHERE p.merchantId = :merchantId ORDER BY p.createdAt DESC")
    List<Payment> findByMerchantIdOrderByCreatedAtDesc(@Param("merchantId") String merchantId);
}

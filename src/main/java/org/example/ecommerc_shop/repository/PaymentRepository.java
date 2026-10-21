package org.example.ecommerc_shop.repository;

import org.example.ecommerc_shop.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findFirstByTransactionId(String transactionId);
}

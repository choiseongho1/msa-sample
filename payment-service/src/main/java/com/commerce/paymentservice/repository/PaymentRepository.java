package com.commerce.paymentservice.repository;

import com.commerce.paymentservice.entity.Payment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long>  {

    Optional<Payment> findByOrderId(String orderId);
}
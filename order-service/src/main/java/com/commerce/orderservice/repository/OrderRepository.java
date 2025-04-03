package com.commerce.orderservice.repository;

import com.commerce.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> , OrderRepositoryDsl {
}
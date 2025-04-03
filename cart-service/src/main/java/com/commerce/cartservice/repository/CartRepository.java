package com.commerce.cartservice.repository;

import com.commerce.cartservice.entity.Cart;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * explain the Class File
 *
 * @author : choiseongho
 * @fileName : CartRepository.java
 * @since : 2025-02-27
 */
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserId(String userId);
}
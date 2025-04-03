package com.commerce.batchservice.repository;

import com.commerce.batchservice.entity.WishlistDeletedLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistDeletedLogRepository extends JpaRepository<WishlistDeletedLog, Long> {
}
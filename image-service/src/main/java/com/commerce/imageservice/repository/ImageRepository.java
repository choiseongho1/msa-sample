package com.commerce.imageservice.repository;

import com.commerce.imageservice.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * explain the Class File
 *
 * @author : choiseongho
 * @fileName : ImageRepository.java
 * @since : 2025-01-07
 */
public interface ImageRepository extends JpaRepository<Image, Long> {
}

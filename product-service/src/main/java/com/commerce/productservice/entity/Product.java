package com.commerce.productservice.entity;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;

/**
 * 상품 Table Entity
 *
 * @author : choiseongho
 * @fileName : Product.java
 * @since : 2025-01-03
 */
@Entity 
@Builder
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter @Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id", nullable = false)
    private Long id; // 상품 ID

    @Column(name = "product_title", nullable = false, length = 255)
    private String title; // 상품명

    @Column(name = "product_image")
    private Long imageId; // 상품 대표 이미지 (이미지 ID로 참조)

    @Column(name = "product_content", nullable = false, columnDefinition = "TEXT")
    private String content; // 상품 설명

    @Enumerated(EnumType.STRING)
    @Column(name = "product_status", nullable = false)
    private ProductStatus status; // 판매 상태 (판매중, 품절, 판매예정 등)

    @Column(name = "product_wishlist_count", nullable = false)
    @Builder.Default
    private Integer wishlistCount = 0; // 위시리스트 수

    @Column(name = "product_price", nullable = false)
    private Integer price; // 상품 가격

    @Enumerated(EnumType.STRING)
    @Column(name = "product_category", nullable = false)
    private ProductCategory category; // 카테고리 (식품, 리빙, 건강 등)

    @Column(name = "product_start_date", nullable = false)
    private LocalDateTime startDate; // 상품 판매 시작일

    @Column(name = "product_created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 상품 생성일

    @Column(name = "product_updated_at", nullable = false)
    private LocalDateTime updatedAt; // 상품 업데이트일

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductOption> productOptions = new ArrayList<>();  // 상품 옵션 목록

    // Enum 클래스 정의
    public enum ProductStatus {
        AVAILABLE, // 판매중
        OUT_OF_STOCK, // 품절
        UPCOMING // 판매예정
    }

    public enum ProductCategory {
        FOOD, // 식품
        LIVING, // 리빙
        HEALTH // 건강
    }
}
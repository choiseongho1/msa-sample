package com.commerce.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 주문상품 Table Entity
 *
 * @author : choiseongho
 * @fileName : OrderProduct.java
 * @since : 2025-01-09
 */
@Entity 
@Builder
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter @Table(name = "order_product")
public class OrderProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 주문상품ID

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order; // 주문정보

    @Column(name = "product_id", nullable = false)
    private Long productId; // 상품ID

    @Column(name = "order_product_quantity", nullable = false)
    private Integer orderProductQuantity; // 수량

    @Column(name = "order_product_price", nullable = false)
    private Integer orderProductPrice; // 상품 가격

    @Column(name = "order_product_option")
    private Long orderProductOption; // 상품 옵션

    @Column(name = "order_product_option_price")
    private Integer orderProductOptionPrice; // 상품 옵션 가격

    @Column(name = "order_product_created_at", nullable = false)
    private LocalDateTime orderProductCreatedAt; // 주문상품 생성일

}


package com.commerce.productservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "wishlist")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wishlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wishlist_id", nullable = false)
    private Long wishlistId; // 위시리스트 ID

    @Column(name = "user_id", nullable = false)
    private String userId;     // 사용자 ID

    @Column(name = "product_id", nullable = false)
    private Long productId;  // 상품 ID

    @Column(name = "wishlist_created_at", nullable = false)
    private LocalDateTime wishlistCreatedAt; // 위시리스트 생성 시간
}

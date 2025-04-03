package com.commerce.productservice.dto;

import java.time.LocalDateTime;

import com.commerce.productservice.entity.Product.ProductCategory;
import com.commerce.productservice.entity.Product.ProductStatus;
import com.querydsl.core.annotations.QueryProjection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Builder
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ProductListDto {


    private Long id; // 상품 ID

    private String title; // 상품명

    private Long imageId; // 상품 대표 이미지 (이미지 ID로 참조)

    private String content; // 상품 설명

    private ProductStatus status; // 판매 상태 (판매중, 품절, 판매예정 등)

    private Integer wishlistCount; // 위시리스트 수

    private Integer price; // 상품 가격

    private ProductCategory category; // 카테고리 (식품, 리빙, 건강 등)

    private String imageLob;

    private Boolean isWishlisted = false;


    @QueryProjection
    public ProductListDto(Long id, String title, Long imageId, String content, ProductStatus status,
            Integer wishlistCount, Integer price, ProductCategory category) {
        this.id = id;
        this.title = title;
        this.imageId = imageId;
        this.content = content;
        this.status = status;
        this.wishlistCount = wishlistCount;
        this.price = price;
        this.category = category;
    }
}

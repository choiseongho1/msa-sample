package com.commerce.productservice.dto;

import com.commerce.productservice.entity.Product;
import com.commerce.productservice.entity.ProductOption;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetailDto {

    // 상품 번호
    private Long id;

    // 상품명
    private String title;

    // 상품 내용
    private String content;

    // 가격
    private Integer price;

    // 카테고리
    private String category;

    // 상태
    private String status;

    // 판매시작일자
    private LocalDateTime startDate;

    // 상품 옵션 List
    private List<ProductOptionDto> options;

    private String imageLob;

    public static ProductDetailDto fromEntity(Product product, String imageLob) {
        return ProductDetailDto.builder()
            .id(product.getId())
            .title(product.getTitle())
            .content(product.getContent())
            .price(product.getPrice())
            .category(product.getCategory().name())  // Enum -> String 변환
            .status(product.getStatus().name())  // Enum -> String 변환
            .startDate(product.getStartDate())
            .options(product.getProductOptions().stream()
                .map(ProductOptionDto::fromEntity)
                .collect(Collectors.toList()))
            .imageLob(imageLob)
            .build();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductOptionDto {

        // 옵션 ID
        private Long id;

        // 옵션명
        private String name;

        // 재고
        private Integer stock;

        // 가격
        private Integer price;

        public static ProductOptionDto fromEntity(ProductOption option) {
            return ProductOptionDto.builder()
                .id(option.getId())
                .name(option.getName())
                .stock(option.getStock())
                .price(option.getPrice())
                .build();
        }
    }
}
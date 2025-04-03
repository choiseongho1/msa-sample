package com.commerce.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductOptionDto {

    // 옵션 ID
    private Long id;

    // 옵션명
    private String name;

    // 재고
    private Integer stock;

    // 가격
    private Integer price;
}
package com.commerce.productservice.dto;


import com.commerce.productservice.entity.ProductOption;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ProductOptionSaveDto {
    @NotBlank(message = "옵션명은 필수입니다")
    @Schema(description = "옵션명", defaultValue = "색상 - 브라운")
    private String name;

    @Min(value = 0, message = "재고는 0 이상이어야 합니다")
    @Schema(description = "재고", defaultValue = "10")
    private Integer stock;

    @Min(value = 0, message = "추가 가격은 0 이상이어야 합니다")
    @Schema(description = "추가 가격", defaultValue = "1000")
    private Integer price;

    public ProductOption toEntity() {
        return ProductOption.builder()
                .name(name)
                .stock(stock)
                .price(price)
                .build();
    }
}
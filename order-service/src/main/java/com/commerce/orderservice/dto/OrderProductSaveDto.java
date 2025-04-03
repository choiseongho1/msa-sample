package com.commerce.orderservice.dto;

import com.commerce.orderservice.entity.OrderProduct;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import lombok.Setter;

/**
* explain the Class File
*
* @fileName     : OrderProductSaveDto.java
* @author       : choiseongho
* @since        : 2025-01-10
*/
@Builder
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class OrderProductSaveDto {

    @Schema(description = "상품ID", example = "1")
    @NotBlank(message = "상품ID는 필수입니다.")
    private Long productId; // 상품ID

    @Schema(description = "상품수량", example = "1")
    @NotBlank(message = "상품수량은 필수입니다.")
    private Integer orderProductQuantity; // 수량

    @Schema(description = "상품가격", example = "1200000")
    private Integer orderProductPrice; // 상품 가격

    @Schema(description = "상품옵션", example = "1")
    private Long orderProductOption; // 상품 옵션

    @Schema(description = "상품옵션가격", example = "1000")
    private Integer orderProductOptionPrice; // 상품 옵션 가격


    public OrderProduct toEntity() {
        return OrderProduct.builder()
                .productId(productId)
                .orderProductQuantity(orderProductQuantity)
                .orderProductPrice(orderProductPrice)
                .orderProductOption(orderProductOption)
                .orderProductOptionPrice(orderProductOptionPrice)
                .orderProductCreatedAt(LocalDateTime.now())
                .build();
    }
}

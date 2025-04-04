package com.commerce.cartservice.dto;

import com.commerce.cartservice.entity.CartItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItemSaveDto {

    @Schema(description = "상품 ID", example = "1")
    private Long productId;

    @Schema(description = "상품 옵션 ID", example = "1")
    private Long productOptionId;

    @Schema(description = "상품 수량", example = "1")
    private int quantity;

    public CartItem toEntity() {
        return CartItem.builder()
                .productId(productId)
                .productOptionId(productOptionId)
                .quantity(quantity)
                .build();
    }

}

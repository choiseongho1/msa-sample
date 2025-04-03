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
public class CartItemUpdateQuantityDto {

    @Schema(description = "장바구니 상품 ID", example = "1")
    private Long cartItemId;

    @Schema(description = "상품 수량", example = "1")
    private int quantity;

}

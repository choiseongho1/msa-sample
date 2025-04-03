package com.commerce.cartservice.dto;

import com.commerce.cartservice.entity.CartItem;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuestCartItemListDto {
    private Long cartItemId;
    private Long productId;
    private Long productOptionId;
    private int quantity;

    // 엔티티를 DTO로 변환하는 정적 메서드
    public static GuestCartItemListDto of(CartItem cartItem) {
        return GuestCartItemListDto.builder()
            .cartItemId(cartItem.getId())
            .productId(cartItem.getProductId())
            .productOptionId(cartItem.getProductOptionId())
            .quantity(cartItem.getQuantity())
            .build();
    }
}

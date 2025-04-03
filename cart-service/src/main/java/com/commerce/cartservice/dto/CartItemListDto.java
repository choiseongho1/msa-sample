package com.commerce.cartservice.dto;


import com.commerce.cartservice.entity.CartItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class CartItemListDto {
    private Long cartItemId;

    private Long productId;

    private Long productOptionId;

    private String productTitle;

    private String optionName;

    private Integer price;

    private String imageLob;

    private int quantity;  // 상품 개수


    public static CartItemListDto of(CartItem cartItem, ProductOptionDetailDto productOptionDetailDto) {
        return CartItemListDto.builder()
            .cartItemId(cartItem.getId())
            .productId(cartItem.getProductId())
            .productOptionId(cartItem.getProductOptionId())
            .productTitle(productOptionDetailDto.getProductTitle())
            .optionName(productOptionDetailDto.getOptionName())
            .price(productOptionDetailDto.getPrice())
            .imageLob(productOptionDetailDto.getImageLob())
            .quantity(cartItem.getQuantity())
            .build();
    }


}
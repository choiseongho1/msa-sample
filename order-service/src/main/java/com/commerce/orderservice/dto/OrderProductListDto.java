package com.commerce.orderservice.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderProductListDto {

    private Long productId; // 상품ID

    private Integer orderProductQuantity; // 수량

    private Integer orderProductPrice; // 상품 가격

    private Long orderProductOption; // 상품 옵션

    private Integer orderProductOptionPrice; // 상품 옵션 가격

    private String imageLob ; // 이미지정보


    @QueryProjection
    public OrderProductListDto(Long productId, Integer orderProductQuantity, Integer orderProductPrice, Long orderProductOption, Integer orderProductOptionPrice) {
        this.productId = productId;
        this.orderProductQuantity = orderProductQuantity;
        this.orderProductPrice = orderProductPrice;
        this.orderProductOption = orderProductOption;
        this.orderProductOptionPrice = orderProductOptionPrice;
    }

}

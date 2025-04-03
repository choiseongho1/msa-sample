package com.commerce.productservice.dto;

import com.commerce.productservice.entity.Wishlist;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder @NoArgsConstructor
public class WishlistListDto {

    private Long wishlistId;

    private Long productId;

    private String productTitle;

    private Integer price;

    @QueryProjection
    public WishlistListDto(Long wishlistId, Long productId, String productTitle, Integer price){
        this.wishlistId = wishlistId;
        this.productId = productId;
        this.productTitle = productTitle;
        this.price = price;
    }

}

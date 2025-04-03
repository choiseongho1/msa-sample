package com.commerce.productservice.dto;

import com.commerce.productservice.entity.Wishlist;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WishlistSaveDto {

    @NotNull(message = "사용자 ID는 필수입니다")
    @Schema(description = "사용자 ID", example = "1")
    private String userId;

    @NotNull(message = "상품 ID는 필수입니다")
    @Schema(description = "상품 ID", example = "1")
    private Long productId;


    public Wishlist toEntity() {
        return Wishlist.builder()
            .userId(userId)
            .productId(productId)
            .wishlistCreatedAt(LocalDateTime.now())
            .build();
    }
}

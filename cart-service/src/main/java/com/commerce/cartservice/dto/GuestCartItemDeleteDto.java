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
public class GuestCartItemDeleteDto {

    @Schema(description = "Session ID", example = "Session ID1")
    private String sessionId;

    @Schema(description = "상품 ID", example = "1")
    private Long productId;

    @Schema(description = "상품 옵션 ID", example = "1")
    private Long productOptionId;


}

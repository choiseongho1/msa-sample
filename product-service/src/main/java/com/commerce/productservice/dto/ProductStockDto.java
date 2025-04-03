package com.commerce.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStockDto {
    private String eventId;
    private Long productId;
    private Long optionId;
    private int quantity;
    private Long orderId;
    private LocalDateTime timestamp;
}
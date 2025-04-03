package com.commerce.orderservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEventDto {
    private String paymentId;
    private String orderId;
    private String userId;
    private BigDecimal amount;
    private LocalDateTime timestamp;
}
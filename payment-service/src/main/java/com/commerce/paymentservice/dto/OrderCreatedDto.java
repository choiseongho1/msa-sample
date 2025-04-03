package com.commerce.paymentservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OrderCreatedDto {
    private String orderId;
    private String userId;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;

}
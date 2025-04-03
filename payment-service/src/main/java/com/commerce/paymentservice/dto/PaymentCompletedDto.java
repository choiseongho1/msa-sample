package com.commerce.paymentservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class PaymentCompletedDto {
    private String paymentId;
    private String orderId;
    private String userId;
    private BigDecimal amount;
    private LocalDateTime paidAt;

}
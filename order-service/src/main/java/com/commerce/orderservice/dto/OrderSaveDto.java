package com.commerce.orderservice.dto;

import com.commerce.orderservice.entity.Order;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 주문정보 저장 DTO
 *
 * @author : choiseongho
 * @fileName : OrderSaveDto.java
 * @since : 2025-01-09
 */
@Getter @Setter
@Builder
@AllArgsConstructor @NoArgsConstructor
public class OrderSaveDto {


    @Schema(description = "회원ID", example = "testID")
    @NotBlank(message = "회원ID는 필수입니다")
    private String userId; // 회원 ID

    @Schema(description = "주문날짜")
    private LocalDateTime orderDate; // 주문 날짜

    @Schema(description = "주문상태")
    private Order.OrderStatus orderStatus; // 주문 상태

    @Schema(description = "배송비")
    private Integer orderShippingFee; // 배송비

    @Schema(description = "결제총액")
    private Integer orderPrice; // 결제총액

    @Schema(description = "카드사")
    private String orderCardCompany; // 카드사

    @Schema(description = "받는분 성함", example = "최성호")
    @NotBlank(message = "받는분 성함은 필수입니다")
    private String orderUsername; // 받는분 성함

    @Schema(description = "우편번호", example = "12345")
    private String orderZipcode; // 우편번호

    @Schema(description = "주소", example = "테스트 주소")
    private String orderAddress; // 주소

    @Schema(description = "전화번호", example = "01012345678")
    private String orderPhone; // 전화번호

    @Schema(description = "주문 요청사항")
    private String orderReq; // 주문 요청사항

    private final List<OrderProductSaveDto> orderProductSaveList =  new ArrayList<>();

    public Order toEntity() {
        return Order.builder()
                .userId(userId)
                .orderDate(orderDate)
                .orderStatus(orderStatus)
                .orderShippingFee(orderShippingFee)
                .orderPrice(orderPrice)
                .orderCardCompany(orderCardCompany)
                .orderUsername(orderUsername)
                .orderZipcode(orderZipcode)
                .orderAddress(orderAddress)
                .orderPhone(orderPhone)
                .orderReq(orderReq)
                .orderCreatedAt(LocalDateTime.now())
                .orderUpdatedAt(LocalDateTime.now())
                .build();
    }
}



package com.commerce.orderservice.dto;

import com.commerce.orderservice.entity.Order;
import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Builder
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class OrderListByUserDto {

    private Long orderId; // 주문ID

    private LocalDateTime orderDate; // 주문 날짜

    private Order.OrderStatus orderStatus; // 주문 상태

    private Integer orderShippingFee; // 배송비

    private Integer orderPrice; // 결제총액

    private String orderCardCompany; // 카드사

    private List<OrderProductListDto> orderProductList; // 주문 상품>

    @QueryProjection
    public OrderListByUserDto(Long orderId, LocalDateTime orderDate, Order.OrderStatus orderStatus, Integer orderShippingFee, Integer orderPrice, String orderCardCompany ) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.orderShippingFee = orderShippingFee;
        this.orderPrice = orderPrice;
        this.orderCardCompany = orderCardCompany;
    }

}

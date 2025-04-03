package com.commerce.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 Table Entity
 *
 * @author : choiseongho
 * @fileName : Order.java
 * @since : 2025-01-09
 */
@Entity 
@Builder
@AllArgsConstructor @NoArgsConstructor
@Getter @Setter @Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 주문 ID

    // 사용자 ID가 아닌
    @Column(name = "user_id", nullable = false)
    private String userId; // 회원 ID

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate; // 주문 날짜

    @Column(name = "order_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus; // 주문 상태

    @Column(name = "order_shipping_fee", nullable = false)
    private Integer orderShippingFee; // 배송비

    @Column(name = "order_price", nullable = false)
    private Integer orderPrice; // 결제총액

    @Column(name = "order_card_company", nullable = false)
    private String orderCardCompany; // 카드사

    @Column(name = "order_username", nullable = false)
    private String orderUsername; // 받는분 성함

    @Column(name = "order_zipcode", nullable = false)
    private String orderZipcode; // 우편번호

    @Column(name = "order_address", nullable = false)
    private String orderAddress; // 받는분주소

    @Column(name = "order_phone", nullable = false)
    private String orderPhone; // 받는분 전화번호

    @Column(name = "order_req")
    private String orderReq; // 배송요청사항

    @Column(name = "order_created_at", nullable = false)
    private LocalDateTime orderCreatedAt; // 주문 생성일

    @Column(name = "order_updated_at", nullable = false)
    private LocalDateTime orderUpdatedAt; // 주문 수정일

    @OneToMany(mappedBy = "order")
    private List<OrderProduct> orderProducts; // 주문상품 연관관계

    public enum OrderStatus {
        // 주문 생성 후 결제 대기
        WAITING_FOR_PAYMENT,
        // 결제 완료
        PAYMENT_COMPLETED,
        // 배송 준비 중
        PREPARING_FOR_SHIPPING,
        // 배송 중
        SHIPPING,
        // 배송 완료
        DELIVERED,
        // 주문 취소
        CANCELED,
        // 반품 신청
        RETURN_REQUESTED,
        // 반품 완료
        RETURN_COMPLETED,
        // 재고 부족
        OUT_OF_STOCK
    }


}


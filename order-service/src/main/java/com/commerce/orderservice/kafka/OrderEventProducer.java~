package com.commerce.orderservice.kafka;

import com.commerce.common.kafka.AbstractEventProducer;
import com.commerce.orderservice.dto.OrderCancelledDto;
import com.commerce.orderservice.dto.OrderCreatedDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 주문 이벤트 Producer
 *
 * @author : choiseongho
 * @fileName : OrderEventProducer.java
 * @since : 2025-03-14
 */
@Slf4j
@Component
public class OrderEventProducer extends AbstractEventProducer {

    @Value("${kafka.topic.order-events}")
    private String orderEventsTopic;

    public OrderEventProducer(KafkaTemplate<String, String> kafkaTemplate,
        ObjectMapper objectMapper) {
        super(kafkaTemplate, objectMapper, "order-service");
    }

    /**
     * 주문 생성 이벤트 발행
     *
     * @param orderId 주문 ID
     * @param userId 사용자 ID
     * @param totalAmount 총 주문 금액
     */
    public void publishOrderCreatedEvent(Long orderId, String userId, int totalAmount) {
        OrderCreatedDto event = OrderCreatedDto.builder()
            .orderId(orderId.toString())
            .userId(userId)
            .totalAmount(totalAmount)
            .build();

        publishEvent(orderEventsTopic, "ORDER_CREATED", event);
        log.info("주문 생성 kafka 발행 성공 order ID: {}", orderId);
    }

    /**
     * 주문 취소 이벤트 발행
     *
     * @param orderId 주문 ID
     */
    public void publishOrderCancelledEvent(Long orderId) {
        OrderCancelledDto event = OrderCancelledDto.builder()
            .orderId(orderId.toString())
            .cancelledAt(LocalDateTime.now())
            .build();

        publishEvent(orderEventsTopic, "ORDER_CANCELLED", event);
        log.info("주문 취소 kafka 발행 성공 order ID: {}", orderId);
    }
}
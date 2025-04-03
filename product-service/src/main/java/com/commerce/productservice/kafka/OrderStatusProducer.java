package com.commerce.productservice.kafka;

import com.commerce.common.kafka.AbstractEventProducer;
import com.commerce.productservice.dto.OrderStatusDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 주문 상태 업데이트 이벤트 발행 클래스
 *
 * @author : choiseongho
 * @fileName : OrderStatusProducer.java
 * @since : 2025-02-24
 */
@Component
@Slf4j
public class OrderStatusProducer extends AbstractEventProducer {

    @Value("${kafka.topic.order-status-update}")
    private String orderStatusTopic;

    public OrderStatusProducer(KafkaTemplate<String, String> kafkaTemplate,
        ObjectMapper objectMapper) {
        super(kafkaTemplate, objectMapper, "product-service");
    }

    public void sendOrderStatusUpdateEvent(Long orderId, String status) {
        OrderStatusDto statusDto = OrderStatusDto.builder()
            .orderId(orderId)
            .status(status)
            .build();

        publishEvent(orderStatusTopic, "STATUS_UPDATE", statusDto);
        log.info("주문 상태 업데이트 이벤트 발행: orderId={}, status={}", orderId, status);
    }
}
package com.commerce.orderservice.kafka;

import com.commerce.common.kafka.AbstractEventProducer;
import com.commerce.orderservice.dto.ProductStockDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 상품 재고 관련 Kafka 이벤트 발행 클래스
 */
@Slf4j
@Component
public class ProductStockProducer extends AbstractEventProducer {

    @Value("${kafka.topic.product-stock-update}")
    private String productStockTopic;

    // 이벤트 타입 상수 정의
    public static final String EVENT_TYPE_INCREASE = "INCREASE";
    public static final String EVENT_TYPE_DECREASE = "DECREASE";

    public ProductStockProducer(KafkaTemplate<String, String> kafkaTemplate,
        ObjectMapper objectMapper) {
        super(kafkaTemplate, objectMapper, "order-service");
    }

    /**
     * 상품 재고 증가 이벤트 발행 (주문 취소 시 사용)
     *
     * @param productId 상품 ID
     * @param optionId 옵션 ID (없을 경우 null 가능)
     * @param quantity 수량
     * @param orderId 주문 ID
     */
    public void increaseStock(Long productId, Long optionId, int quantity, Long orderId) {
        ProductStockDto stockDto = createStockDto(productId, optionId, quantity, orderId);
        publishEvent(productStockTopic, EVENT_TYPE_INCREASE, stockDto);
        log.info("재고 증가 이벤트 발행: productId={}, quantity={}", productId, quantity);
    }

    /**
     * 상품 재고 감소 이벤트 발행 (주문 생성 시 사용)
     *
     * @param productId 상품 ID
     * @param optionId 옵션 ID (없을 경우 null 가능)
     * @param quantity 수량
     * @param orderId 주문 ID
     */
    public void decreaseStock(Long productId, Long optionId, int quantity, Long orderId) {
        ProductStockDto stockDto = createStockDto(productId, optionId, quantity, orderId);
        publishEvent(productStockTopic, EVENT_TYPE_DECREASE, stockDto);
        log.info("재고 감소 이벤트 발행: productId={}, quantity={}", productId, quantity);
    }

    /**
     * 재고 이벤트 DTO 생성 헬퍼 메소드
     */
    private ProductStockDto createStockDto(Long productId, Long optionId, int quantity, Long orderId) {
        return ProductStockDto.builder()
            .eventId(UUID.randomUUID().toString())
            .productId(productId)
            .optionId(optionId)
            .quantity(quantity)
            .orderId(orderId)
            .timestamp(LocalDateTime.now())
            .build();
    }
}
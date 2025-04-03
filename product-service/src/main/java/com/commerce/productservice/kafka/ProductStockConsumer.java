package com.commerce.productservice.kafka;

import com.commerce.common.dto.EventMessageDto;
import com.commerce.common.kafka.AbstractEventConsumer;
import com.commerce.productservice.dto.ProductStockDto;
import com.commerce.productservice.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 상품 재고 업데이트 이벤트 소비자 클래스
 * 재고 증가 및 감소 이벤트를 처리합니다.
 */
@Slf4j
@Component
public class ProductStockConsumer extends AbstractEventConsumer {

    private final ProductService productService;

    // 이벤트 타입 상수 정의
    private static final String EVENT_TYPE_DECREASE = "DECREASE";
    private static final String EVENT_TYPE_INCREASE = "INCREASE";

    public ProductStockConsumer(ObjectMapper objectMapper, ProductService productService) {
        super(objectMapper);
        this.productService = productService;
    }

    @Transactional
    @KafkaListener(topics = "${kafka.topic.product-stock-update}", groupId = "${kafka.consumer.group-id}")
    @Override
    public void consume(String message) { //, Acknowledgment acknowledgment
        log.info("이미지 ID 이벤트 수신: {}", message);
        super.consume(message);
    }



    @Override
    protected void processEvent(EventMessageDto<?> eventMessage) {
        String eventType = eventMessage.getEventType();
        ProductStockDto stockDto = convertPayload(eventMessage, ProductStockDto.class);

        // 이벤트 ID 로깅 (중복 처리 방지용)
//        log.info("Processing stock update event with ID: {}", stockDto.getEventId());

        if (EVENT_TYPE_DECREASE.equals(eventType)) {
            productService.decreaseStock(
                stockDto.getProductId(),
                stockDto.getOptionId(),
                stockDto.getQuantity(),
                stockDto.getOrderId()
            );
        } else if (EVENT_TYPE_INCREASE.equals(eventType)) {
            productService.increaseStock(
                stockDto.getProductId(),
                stockDto.getOptionId(),
                stockDto.getQuantity(),
                stockDto.getOrderId()
            );
        } else {
            log.warn("Order -> Product Stock Kafka 올바르지 않은 상태값: {}", eventType);
        }
    }
}
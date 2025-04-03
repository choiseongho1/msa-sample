package com.commerce.orderservice.kafka;

import com.commerce.common.dto.EventMessageDto;
import com.commerce.common.kafka.AbstractEventConsumer;
import com.commerce.orderservice.dto.OrderStatusDto;
import com.commerce.orderservice.entity.Order;
import com.commerce.orderservice.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderStatusConsumer extends AbstractEventConsumer {

    private final OrderService orderService;

    public OrderStatusConsumer(ObjectMapper objectMapper, OrderService orderService) {
        super(objectMapper);
        this.orderService = orderService;
    }

    @KafkaListener(topics = "${kafka.topic.order-status-update}", groupId = "${kafka.consumer.group-id}")
    @Override
    public void consume(String message) {
        log.info("Product -> OrderService 재고부족 Consumer 실행");
        super.consume(message);
    }

    @Override
    protected void processEvent(EventMessageDto<?> eventMessage) {
        if ("STATUS_UPDATE".equals(eventMessage.getEventType())) {
            OrderStatusDto statusDto = convertPayload(eventMessage, OrderStatusDto.class);
            log.info("주문 상태 업데이트: orderId={}, status={}", statusDto.getOrderId(), statusDto.getStatus());

            orderService.updateOrderStatus(
                statusDto.getOrderId(),
                Order.OrderStatus.valueOf(statusDto.getStatus())
            );
        } else {
            log.warn("지원하지 않는 이벤트 타입: {}", eventMessage.getEventType());
        }
    }
}
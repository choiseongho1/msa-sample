package com.commerce.paymentservice.kafka;

import com.commerce.common.dto.EventMessageDto;
import com.commerce.common.kafka.AbstractEventConsumer;
import com.commerce.paymentservice.dto.OrderCreatedDto;
import com.commerce.paymentservice.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderEventConsumer extends AbstractEventConsumer {

    private final PaymentService paymentService;

    public OrderEventConsumer(ObjectMapper objectMapper, PaymentService paymentService) {
        super(objectMapper);
        this.paymentService = paymentService;
    }

    @KafkaListener(topics = "${kafka.topic.order-events}", groupId = "${kafka.consumer.group-id}")
    @Override
    public void consume(String message) {
        log.info("주문 이벤트 수신: {}", message);
        super.consume(message);
    }

    @Override
    protected void processEvent(EventMessageDto<?> eventMessage) {
        switch (eventMessage.getEventType()) {
            case "ORDER_CREATED":
                OrderCreatedDto orderCreatedEvent = convertPayload(eventMessage, OrderCreatedDto.class);
                processOrderCreated(orderCreatedEvent);
                break;
            case "ORDER_CANCELLED":
                OrderCreatedDto orderCancelledEvent = convertPayload(eventMessage, OrderCreatedDto.class);
                processOrderCancelled(orderCancelledEvent);
                break;
            default:
                log.warn("지원하지 않는 이벤트 타입: {}", eventMessage.getEventType());
        }
    }

    private void processOrderCreated(OrderCreatedDto event) {
        paymentService.processPayment(event.getOrderId(), event.getUserId(), event.getTotalAmount());
    }

    private void processOrderCancelled(OrderCreatedDto event) {
        paymentService.cancelPayment(event.getOrderId());
    }
}
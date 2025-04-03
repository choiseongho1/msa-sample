package com.commerce.orderservice.kafka;

import com.commerce.common.dto.EventMessageDto;
import com.commerce.common.kafka.AbstractEventConsumer;
import com.commerce.orderservice.dto.PaymentEventDto;
import com.commerce.orderservice.entity.Order;
import com.commerce.orderservice.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PaymentEventConsumer extends AbstractEventConsumer {

    private final OrderService orderService;

    public PaymentEventConsumer(ObjectMapper objectMapper, OrderService orderService) {
        super(objectMapper);
        this.orderService = orderService;
    }

    @KafkaListener(topics = "${kafka.topic.payment-events}", groupId = "${kafka.consumer.group-id}")
    @Override
    public void consume(String message) {
        log.info("Payment -> Order 결제 후 처리 Consumer 실행");
        super.consume(message);
    }

    @Override
    protected void processEvent(EventMessageDto<?> eventMessage) {
        switch (eventMessage.getEventType()) {
            case "PAYMENT_COMPLETED":
                PaymentEventDto completedEvent = convertPayload(eventMessage, PaymentEventDto.class);
                handlePaymentCompleted(completedEvent);
                break;
            case "PAYMENT_FAILED":
                PaymentEventDto failedEvent = convertPayload(eventMessage, PaymentEventDto.class);
                handlePaymentFailed(failedEvent);
                break;
            case "PAYMENT_CANCELLED":
                PaymentEventDto cancelledEvent = convertPayload(eventMessage, PaymentEventDto.class);
                handlePaymentCancelled(cancelledEvent);
                break;
            default:
                log.warn("올바르지 않은 결제상태 event type: {}", eventMessage.getEventType());
        }
    }

    private void handlePaymentCompleted(PaymentEventDto event) {
        log.info("결제 성공 order ID: {}", event.getOrderId());

        // Update order status to PAYMENT_COMPLETED
        orderService.updateOrderStatus(
            Long.valueOf(event.getOrderId()),
            Order.OrderStatus.PAYMENT_COMPLETED
        );
    }

    private void handlePaymentFailed(PaymentEventDto event) {
        log.info("결제 실패 order ID: {}", event.getOrderId());

        orderService.updateOrderStatus(
            Long.valueOf(event.getOrderId()),
            Order.OrderStatus.CANCELED
        );

    }

    private void handlePaymentCancelled(PaymentEventDto event) {
        log.info("주문 취소 order ID: {}", event.getOrderId());

        // Update order status to CANCELED
        orderService.updateOrderStatus(
            Long.valueOf(event.getOrderId()),
            Order.OrderStatus.CANCELED
        );
    }
}
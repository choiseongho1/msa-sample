package com.commerce.paymentservice.kafka;

import com.commerce.common.kafka.AbstractEventProducer;
import com.commerce.paymentservice.dto.PaymentCompletedDto;
import com.commerce.paymentservice.dto.PaymentFailedDto;
import com.commerce.paymentservice.entity.Payment;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PaymentEventProducer extends AbstractEventProducer {

    @Value("${kafka.topic.payment-events}")
    private String paymentEventsTopic;

    public PaymentEventProducer(KafkaTemplate<String, String> kafkaTemplate,
        ObjectMapper objectMapper) {
        super(kafkaTemplate, objectMapper, "payment-service");
    }

    public void publishPaymentCompletedEvent(Payment payment) {
        PaymentCompletedDto event = PaymentCompletedDto.builder()
            .paymentId(payment.getPaymentId())
            .orderId(payment.getOrderId())
            .userId(payment.getUserId())
            .amount(payment.getAmount())
            .paidAt(payment.getPaidAt())
            .build();

        publishEvent(paymentEventsTopic, "PAYMENT_COMPLETED", event);
    }


    public void publishPaymentFailedEvent(Payment payment) {
        log.error("Payment -> Order 결제 후 처리(실패) Producer 실행");

        PaymentFailedDto failedEvent = PaymentFailedDto.builder()
            .paymentId(payment.getPaymentId())
            .orderId(payment.getOrderId())
            .userId(payment.getUserId())
            .amount(payment.getAmount())
            .failedAt(LocalDateTime.now())
            .build();

        publishEvent(paymentEventsTopic, "PAYMENT_FAILED", failedEvent);

        log.error("결제 실패 Kafka 발행 orderId: {}", payment.getOrderId());
    }


    public void publishPaymentCancelledEvent(Payment payment) {
        // 결제 취소 이벤트 발행 로직
    }
}
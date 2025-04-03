package com.commerce.productservice.kafka;

import com.commerce.common.dto.EventMessageDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderStatusProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OrderStatusProducer orderStatusProducer;

    private final String ORDER_STATUS_TOPIC = "order-status-topic";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(orderStatusProducer, "orderStatusTopic", ORDER_STATUS_TOPIC);
    }

    @Test
    @DisplayName("주문 상태 변경 이벤트 전송 성공 테스트")
    void sendOrderStatusUpdateEvent_Success() throws JsonProcessingException {
        // Given
        Long orderId = 1L;
        String newStatus = "PROCESSING";
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        future.complete(null); // 성공 시나리오

        lenient().when(objectMapper.writeValueAsString(any())).thenReturn("serializedEvent");
        lenient().when(kafkaTemplate.send(eq(ORDER_STATUS_TOPIC), anyString())).thenReturn(future);

        // When
        orderStatusProducer.sendOrderStatusUpdateEvent(orderId, newStatus);

        // Then
        verify(objectMapper).writeValueAsString(any(EventMessageDto.class));
        verify(kafkaTemplate).send(eq(ORDER_STATUS_TOPIC), anyString());
    }

    @Test
    @DisplayName("주문 상태 변경 이벤트 전송 시 직렬화 실패 테스트")
    void sendOrderStatusUpdateEvent_SerializationFailed() throws JsonProcessingException {
        // Given
        Long orderId = 1L;
        String newStatus = "PROCESSING";

        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("Serialization failed") {});

        // When & Then
        assertThrows(RuntimeException.class, () -> orderStatusProducer.sendOrderStatusUpdateEvent(orderId, newStatus));
        verify(objectMapper).writeValueAsString(any(EventMessageDto.class));
    }

    @Test
    @DisplayName("주문 상태 변경 이벤트 전송 시 Kafka 전송 실패 테스트")
    void sendOrderStatusUpdateEvent_KafkaPublishFailed() throws JsonProcessingException {
        // Given
        Long orderId = 1L;
        String newStatus = "PROCESSING";
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka send failed"));

        lenient().when(objectMapper.writeValueAsString(any())).thenReturn("serializedEvent");
        lenient().when(kafkaTemplate.send(eq(ORDER_STATUS_TOPIC), anyString())).thenReturn(future);

        // When & Then
        assertThrows(RuntimeException.class, () -> orderStatusProducer.sendOrderStatusUpdateEvent(orderId, newStatus));
        verify(objectMapper).writeValueAsString(any(EventMessageDto.class));
        verify(kafkaTemplate).send(eq(ORDER_STATUS_TOPIC), anyString());
    }
}
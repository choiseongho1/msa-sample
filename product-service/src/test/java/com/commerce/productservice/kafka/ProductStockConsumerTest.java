package com.commerce.productservice.kafka;

import com.commerce.common.dto.EventMessageDto;
import com.commerce.productservice.dto.ProductStockDto;
import com.commerce.productservice.service.ProductService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductStockConsumerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductStockConsumer productStockConsumer;

    @Test
    @DisplayName("consume - 재고 감소 이벤트 처리 성공")
    void testConsumeDecreaseStockEvent() throws Exception {
        // Given
        String eventType = "DECREASE";
        Long productId = 1L;
        Long optionId = 2L;
        Integer quantity = 3;
        Long orderId = 100L;

        String message = "{\"eventType\":\"" + eventType + "\",\"payload\":{}}";

        ProductStockDto stockDto = new ProductStockDto();
        stockDto.setProductId(productId);
        stockDto.setOptionId(optionId);
        stockDto.setQuantity(quantity);
        stockDto.setOrderId(orderId);

        EventMessageDto<Object> eventMessageDto = new EventMessageDto<>();
        eventMessageDto.setEventType(eventType);
        eventMessageDto.setPayload(stockDto);

        // Setup mocks with lenient mode
        lenient().when(objectMapper.readValue(eq(message), any(TypeReference.class))).thenReturn(eventMessageDto);
        lenient().when(objectMapper.convertValue(any(), eq(ProductStockDto.class))).thenReturn(stockDto);
        doNothing().when(productService).decreaseStock(anyLong(), anyLong(), anyInt(), anyLong());

        // When
        productStockConsumer.consume(message);

        // Then
        verify(productService).decreaseStock(eq(productId), eq(optionId), eq(quantity), eq(orderId));
    }

    @Test
    @DisplayName("consume - 재고 증가 이벤트 처리 성공")
    void testConsumeIncreaseStockEvent() throws Exception {
        // Given
        String eventType = "INCREASE";
        Long productId = 1L;
        Long optionId = 2L;
        Integer quantity = 3;
        Long orderId = 100L;

        String message = "{\"eventType\":\"" + eventType + "\",\"payload\":{}}";

        ProductStockDto stockDto = new ProductStockDto();
        stockDto.setProductId(productId);
        stockDto.setOptionId(optionId);
        stockDto.setQuantity(quantity);
        stockDto.setOrderId(orderId);

        EventMessageDto<Object> eventMessageDto = new EventMessageDto<>();
        eventMessageDto.setEventType(eventType);
        eventMessageDto.setPayload(stockDto);

        // Setup mocks with lenient mode
        lenient().when(objectMapper.readValue(eq(message), any(TypeReference.class))).thenReturn(eventMessageDto);
        lenient().when(objectMapper.convertValue(any(), eq(ProductStockDto.class))).thenReturn(stockDto);
        doNothing().when(productService).increaseStock(anyLong(), anyLong(), anyInt(), anyLong());

        // When
        productStockConsumer.consume(message);

        // Then
        verify(productService).increaseStock(eq(productId), eq(optionId), eq(quantity), eq(orderId));
    }

    @Test
    @DisplayName("consume - 잘못된 이벤트 타입 처리")
    void testConsumeInvalidEventType() throws Exception {
        // Given
        String eventType = "INVALID_TYPE";
        Long productId = 1L;
        Long optionId = 2L;
        Integer quantity = 3;
        Long orderId = 100L;

        String message = "{\"eventType\":\"" + eventType + "\",\"payload\":{}}";

        ProductStockDto stockDto = new ProductStockDto();
        stockDto.setProductId(productId);
        stockDto.setOptionId(optionId);
        stockDto.setQuantity(quantity);
        stockDto.setOrderId(orderId);

        EventMessageDto<Object> eventMessageDto = new EventMessageDto<>();
        eventMessageDto.setEventType(eventType);
        eventMessageDto.setPayload(stockDto);

        // Setup mocks with lenient mode
        lenient().when(objectMapper.readValue(eq(message), any(TypeReference.class))).thenReturn(eventMessageDto);
        lenient().when(objectMapper.convertValue(any(), eq(ProductStockDto.class))).thenReturn(stockDto);

        // When
        productStockConsumer.consume(message);

        // Then
        verify(productService, never()).decreaseStock(anyLong(), anyLong(), anyInt(), anyLong());
        verify(productService, never()).increaseStock(anyLong(), anyLong(), anyInt(), anyLong());
    }

    @Test
    @DisplayName("consume - 예외 발생 시 처리")
    void testConsumeWithException() throws Exception {
        // Given
        String message = "{\"eventType\":\"DECREASE\",\"payload\":{}}";

        // Setup mocks to throw exception
        when(objectMapper.readValue(eq(message), any(TypeReference.class))).thenThrow(new RuntimeException("Test exception"));

        // When
        productStockConsumer.consume(message);

        // Then
        verify(productService, never()).decreaseStock(anyLong(), anyLong(), anyInt(), anyLong());
        verify(productService, never()).increaseStock(anyLong(), anyLong(), anyInt(), anyLong());
    }
}
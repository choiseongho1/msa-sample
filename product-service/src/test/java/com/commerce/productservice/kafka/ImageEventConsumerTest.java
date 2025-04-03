package com.commerce.productservice.kafka;

import com.commerce.common.dto.EventMessageDto;
import com.commerce.common.exception.CustomException;
import com.commerce.productservice.dto.ImageIdDto;
import com.commerce.productservice.entity.Product;
import com.commerce.productservice.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageEventConsumerTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ImageEventConsumer imageEventConsumer;

    @Test
    @DisplayName("IMAGE_ID_CREATED 이벤트 처리 - 성공")
    void processEventImageIdCreatedSuccess() throws Exception {
        // Given
        Long productId = 1L;
        Long imageId = 100L;
        ImageIdDto imageIdDto = new ImageIdDto(productId, imageId);
        EventMessageDto<ImageIdDto> eventMessage = new EventMessageDto<>();
        eventMessage.setEventType("IMAGE_ID_CREATED");
        eventMessage.setPayload(imageIdDto);

        Product product = new Product();
        product.setId(productId);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(objectMapper.convertValue(imageIdDto, ImageIdDto.class)).thenReturn(imageIdDto);

        // When
        imageEventConsumer.processEvent(eventMessage);

        // Then
        verify(productRepository).findById(productId);
        verify(productRepository).save(product);
        assertEquals(imageId, product.getImageId());
    }

    @Test
    @DisplayName("IMAGE_ID_CREATED 이벤트 처리 - 상품을 찾을 수 없음")
    void processEventImageIdCreatedProductNotFound() throws Exception {
        // Given
        Long productId = 999L;
        Long imageId = 100L;
        ImageIdDto imageIdDto = new ImageIdDto(productId, imageId);
        EventMessageDto<ImageIdDto> eventMessage = new EventMessageDto<>();
        eventMessage.setEventType("IMAGE_ID_CREATED");
        eventMessage.setPayload(imageIdDto);

        when(productRepository.findById(productId)).thenReturn(Optional.empty());
        when(objectMapper.convertValue(imageIdDto, ImageIdDto.class)).thenReturn(imageIdDto);

        // When & Then
        assertThrows(CustomException.class, () -> {
            imageEventConsumer.processEvent(eventMessage);
        });
    }

    @Test
    @DisplayName("지원하지 않는 이벤트 타입 처리")
    void processEventUnsupportedEventType() {
        // Given
        EventMessageDto<?> eventMessage = new EventMessageDto<>();
        eventMessage.setEventType("UNSUPPORTED_EVENT");

        // When
        imageEventConsumer.processEvent(eventMessage);

        // Then
        verify(productRepository, never()).findById(any());
        verify(productRepository, never()).save(any());
    }
}
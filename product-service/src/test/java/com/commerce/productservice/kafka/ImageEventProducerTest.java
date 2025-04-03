package com.commerce.productservice.kafka;

import com.commerce.common.dto.EventMessageDto;
import com.commerce.productservice.dto.ImageEventDto;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ImageEventProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ImageEventProducer imageEventProducer;

    @Test
    @DisplayName("sendImageEvent - 이미지 이벤트 전송 성공")
    void testSendImageEvent() throws Exception {
        // Given
        String imageLob = "base64EncodedImageData";
        Long productId = 1L;
        String imageStorageTopic = "image-storage-topic";

        // Set the topic values using reflection
        ReflectionTestUtils.setField(imageEventProducer, "imageStorageTopic", imageStorageTopic);

        // Setup mocks with lenient mode
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        lenient().when(objectMapper.writeValueAsString(any())).thenReturn("serializedEvent");
        lenient().when(kafkaTemplate.send(anyString(), anyString())).thenReturn(future);

        // When
        imageEventProducer.sendImageEvent(imageLob, productId);

        // Then
        verify(kafkaTemplate).send(eq(imageStorageTopic), anyString());
    }

    @Test
    @DisplayName("findImageEvent - 이미지 조회 이벤트 전송 성공")
    void testFindImageEvent() throws Exception {
        // Given
        Long imageId = 1L;
        String imageFindTopic = "image-find-topic";

        // Set the topic values using reflection
        ReflectionTestUtils.setField(imageEventProducer, "imageFindTopic", imageFindTopic);

        // Setup mocks with lenient mode
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        lenient().when(objectMapper.writeValueAsString(any())).thenReturn("serializedEvent");
        lenient().when(kafkaTemplate.send(anyString(), anyString())).thenReturn(future);

        // When
        imageEventProducer.findImageEvent(imageId);

        // Then
        verify(kafkaTemplate).send(eq(imageFindTopic), anyString());
    }
}
package com.commerce.imageservice.kafka;

import com.commerce.common.kafka.AbstractEventProducer;
import com.commerce.imageservice.dto.ImageIdDto;
import com.commerce.imageservice.dto.ImageLobDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * 이미지 관련 이벤트 발행 클래스
 *
 * @author : choiseongho
 * @fileName : ImageProducer.java
 * @since : 2025-01-07
 */
@Service
public class ImageProducer extends AbstractEventProducer {

    @Value("${kafka.topic.image-id}")
    private String imageIdTopic;

    @Value("${kafka.topic.image-lob}")
    private String imageLobTopic;

    public ImageProducer(KafkaTemplate<String, String> kafkaTemplate,
        ObjectMapper objectMapper) {
        super(kafkaTemplate, objectMapper, "image-service");
    }

    /**
     * 이미지 ID 이벤트 발행
     *
     * @param imageId 이미지 ID
     * @param productId 상품 ID
     */
    public void sendImageId(Long imageId, Long productId) {
        ImageIdDto imageIdDto = ImageIdDto.builder()
            .imageId(imageId)
            .productId(productId)
            .build();

        publishEvent(imageIdTopic, "IMAGE_ID_CREATED", imageIdDto);
    }

    /**
     * 이미지 LOB 이벤트 발행
     *
     * @param imageLob 이미지 LOB 데이터
     */
    public void sendImageLob(String imageLob) {
        ImageLobDto imageLobDto = ImageLobDto.builder()
            .imageLob(imageLob)
            .build();

        publishEvent(imageLobTopic, "IMAGE_LOB_SENT", imageLobDto);
    }
}
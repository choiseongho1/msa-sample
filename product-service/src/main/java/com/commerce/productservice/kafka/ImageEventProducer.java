package com.commerce.productservice.kafka;

import com.commerce.common.kafka.AbstractEventProducer;
import com.commerce.productservice.dto.ImageEventDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ImageEventProducer extends AbstractEventProducer {

    @Value("${kafka.topic.image-storage}")
    private String imageStorageTopic;

    @Value("${kafka.topic.image-find}")
    private String imageFindTopic;

    public ImageEventProducer(KafkaTemplate<String, String> kafkaTemplate,
        ObjectMapper objectMapper) {
        super(kafkaTemplate, objectMapper, "product-service");
    }

    public void sendImageEvent(String imageLob, Long productId) {
        ImageEventDto imageEvent = ImageEventDto.builder()
            .productId(productId)
            .imageLob(imageLob)
            .build();

        publishEvent(imageStorageTopic, "IMAGE_UPLOAD", imageEvent);
    }

    public void findImageEvent(Long imageId) {
        publishEvent(imageFindTopic, "IMAGE_FIND", imageId);
    }
}
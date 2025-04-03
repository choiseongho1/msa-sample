package com.commerce.productservice.kafka;

import com.commerce.common.dto.EventMessageDto;
import com.commerce.common.exception.CustomException;
import com.commerce.common.kafka.AbstractEventConsumer;
import com.commerce.productservice.dto.ImageIdDto;
import com.commerce.productservice.entity.Product;
import com.commerce.productservice.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
public class ImageEventConsumer extends AbstractEventConsumer {

    private final ProductRepository productRepository;

    public ImageEventConsumer(ObjectMapper objectMapper, ProductRepository productRepository) {
        super(objectMapper);
        this.productRepository = productRepository;
    }

    @Transactional
    @KafkaListener(topics = "${kafka.topic.image-id}", groupId = "${kafka.consumer.group-id}")
    @Override
    public void consume(String message) {
        log.info("이미지 ID 이벤트 수신: {}", message);
        super.consume(message);
    }

    @Override
    protected void processEvent(EventMessageDto<?> eventMessage) {
        if ("IMAGE_ID_CREATED".equals(eventMessage.getEventType())) {
            ImageIdDto imageIdDto = convertPayload(eventMessage, ImageIdDto.class);
            updateProductWithImageId(imageIdDto.getProductId(), imageIdDto.getImageId());
        } else {
            log.warn("지원하지 않는 이벤트 타입: {}", eventMessage.getEventType());
        }
    }

    private void updateProductWithImageId(Long productId, Long imageId) {
        log.info("image-service로부터 imageId 전송 받음: {}", imageId);

        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setImageId(imageId);
            productRepository.save(product);
            log.info("해당 상품 ProductId {} imageId: {} Update", product.getId(), imageId);
        } else {
            throw new CustomException("ProductId를 찾을 수 없음 " + productId);
        }
    }
}
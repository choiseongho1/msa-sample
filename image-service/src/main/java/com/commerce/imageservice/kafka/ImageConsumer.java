package com.commerce.imageservice.kafka;

import com.commerce.common.dto.EventMessageDto;
import com.commerce.common.kafka.AbstractEventConsumer;
import com.commerce.imageservice.dto.ImageStorageDto;
import com.commerce.imageservice.entity.Image;
import com.commerce.imageservice.repository.ImageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ImageConsumer extends AbstractEventConsumer {

    private final ImageRepository imageRepository;
    private final ImageProducer imageProducer;
    private final ObjectMapper objectMapper;

    public ImageConsumer(ObjectMapper objectMapper,
        ImageRepository imageRepository,
        ImageProducer imageProducer) {
        super(objectMapper);
        this.imageRepository = imageRepository;
        this.imageProducer = imageProducer;
        this.objectMapper = objectMapper;
    }

    @Transactional
    @KafkaListener(topics = "${kafka.topic.image-storage}", groupId = "${kafka.consumer.group-id}")
    @Override
    public void consume(String message) {
        log.info("이미지 저장 이벤트 수신: {}", message);
        super.consume(message);
    }

    @Transactional
    @KafkaListener(topics = "${kafka.topic.image-find}", groupId = "${kafka.consumer.group-id}")
    public void consumeFindImage(String message) {
        log.info("이미지 조회 이벤트 수신: {}", message);
        try {
            EventMessageDto<?> eventMessage = objectMapper.readValue(message, EventMessageDto.class);
            if ("IMAGE_FIND".equals(eventMessage.getEventType())) {
                Long imageId = Long.valueOf(eventMessage.getPayload().toString());
                findAndSendImage(imageId);
            } else {
                log.warn("지원하지 않는 이벤트 타입: {}", eventMessage.getEventType());
            }
        } catch (Exception e) {
            log.error("이미지 조회 이벤트 처리 중 오류 발생", e);
        }
    }

    @Override
    protected void processEvent(EventMessageDto<?> eventMessage) {
        if ("IMAGE_UPLOAD".equals(eventMessage.getEventType())) {
            ImageStorageDto imageStorageDto = convertPayload(eventMessage, ImageStorageDto.class);
            processImageStorage(imageStorageDto);
        } else {
            log.warn("지원하지 않는 이벤트 타입: {}", eventMessage.getEventType());
        }
    }

    private void processImageStorage(ImageStorageDto imageStorageDto) {
        log.info("product-service로부터 ImageLob 전송받음");
        Image savedImage = saveImage(imageStorageDto.getImageLob());
        imageProducer.sendImageId(savedImage.getId(), imageStorageDto.getProductId());
        log.info("이미지 저장 후, product-service로 다시 imageId 전송 ---> {}", savedImage.getId());
    }

    private void findAndSendImage(Long imageId) {
        Image image = imageRepository.findById(imageId)
            .orElseThrow(() -> new IllegalArgumentException("이미지 찾기 실패: " + imageId));
        imageProducer.sendImageLob(image.getImageLob());
        log.info("이미지 조회 후, 요청자에게 imageLob 전송 ---> imageId: {}", imageId);
    }

    private Image saveImage(String imageLob) {
        Image image = new Image();
        image.setImageType(Image.ImageType.THUMBNAIL);
        image.setImageLob(imageLob);
        return imageRepository.save(image);
    }
}
package com.commerce.productservice.feign;

import com.commerce.productservice.config.FeignConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageServiceClientTest {

    @Mock
    private ImageServiceClient imageServiceClient;

    @Test
    @DisplayName("GET /image/{imageId} - 이미지 조회 성공")
    void getImageLobSuccess() {
        // Given
        Long imageId = 1L;
        String expectedImageLob = "base64EncodedImageData";
        when(imageServiceClient.getImageLob(imageId)).thenReturn(expectedImageLob);

        // When
        String actualImageLob = imageServiceClient.getImageLob(imageId);

        // Then
        assertEquals(expectedImageLob, actualImageLob);
    }

    @Test
    @DisplayName("GET /image/{imageId} - 존재하지 않는 이미지 ID로 조회")
    void getImageLobNotFound() {
        // Given
        Long invalidImageId = 999L;
        when(imageServiceClient.getImageLob(invalidImageId)).thenThrow(new RuntimeException("Image not found"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            imageServiceClient.getImageLob(invalidImageId);
        });
    }
}
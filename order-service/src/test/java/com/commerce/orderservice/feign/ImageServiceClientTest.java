package com.commerce.orderservice.feign;

import com.commerce.common.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

class ImageServiceClientTest {

    @Mock
    private ImageServiceClient imageServiceClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("이미지 LOB 조회 테스트 - 성공")
    void getImageLobTest_Success() {
        // given
        Long imageId = 12345L;
        String expectedLob = "image_lob_data";

        given(imageServiceClient.getImageLob(imageId)).willReturn(expectedLob);

        // when
        String result = imageServiceClient.getImageLob(imageId);

        // then
        assertNotNull(result);
        assertEquals(expectedLob, result);
        verify(imageServiceClient, times(1)).getImageLob(imageId);
    }

    @Test
    @DisplayName("이미지 LOB 조회 테스트 - 실패")
    void getImageLobTest_Failure() {
        // given
        Long imageId = 12345L;

        given(imageServiceClient.getImageLob(imageId)).willThrow(new CustomException("이미지 LOB 조회 실패"));

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            imageServiceClient.getImageLob(imageId);
        });

        assertEquals("이미지 LOB 조회 실패", exception.getMessage());
        verify(imageServiceClient, times(1)).getImageLob(imageId);
    }
}
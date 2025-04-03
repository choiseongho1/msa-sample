package com.commerce.orderservice.feign;

import com.commerce.common.exception.CustomException;
import com.commerce.orderservice.dto.ProductOptionDto;
import com.commerce.orderservice.dto.ProductPriceDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

class ProductServiceClientTest {

    @Mock
    private ProductServiceClient productServiceClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("상품 검증 테스트 - 성공")
    void validateProductTest_Success() {
        // given
        Long productId = 1L;

        given(productServiceClient.validateProduct(productId)).willReturn(true);

        // when
        boolean result = productServiceClient.validateProduct(productId);

        // then
        assertTrue(result);
        verify(productServiceClient, times(1)).validateProduct(productId);
    }

    @Test
    @DisplayName("상품 검증 테스트 - 실패")
    void validateProductTest_Failure() {
        // given
        Long productId = 1L;

        given(productServiceClient.validateProduct(productId)).willReturn(false);

        // when
        boolean result = productServiceClient.validateProduct(productId);

        // then
        assertFalse(result);
        verify(productServiceClient, times(1)).validateProduct(productId);
    }

    @Test
    @DisplayName("상품 가격 조회 테스트 - 성공")
    void getProductPriceTest_Success() {
        // given
        Long productId = 1L;
        ProductPriceDto expectedPrice = new ProductPriceDto(1000);

        given(productServiceClient.getProductPrice(productId)).willReturn(expectedPrice);

        // when
        ProductPriceDto result = productServiceClient.getProductPrice(productId);

        // then
        assertNotNull(result);
        assertEquals(expectedPrice.getPrice(), result.getPrice());
        verify(productServiceClient, times(1)).getProductPrice(productId);
    }

    @Test
    @DisplayName("상품 가격 조회 테스트 - 실패")
    void getProductPriceTest_Failure() {
        // given
        Long productId = 1L;

        given(productServiceClient.getProductPrice(productId)).willThrow(new CustomException("상품 가격 조회 실패"));

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            productServiceClient.getProductPrice(productId);
        });

        assertEquals("상품 가격 조회 실패", exception.getMessage());
        verify(productServiceClient, times(1)).getProductPrice(productId);
    }

    @Test
    @DisplayName("상품 옵션 조회 테스트 - 성공")
    void getProductOptionTest_Success() {
        // given
        Long productId = 1L;
        Long optionId = 1L;
        ProductOptionDto expectedOption = ProductOptionDto.builder()
            .price(500)
            .build();

        given(productServiceClient.getProductOption(productId, optionId)).willReturn(expectedOption);

        // when
        ProductOptionDto result = productServiceClient.getProductOption(productId, optionId);

        // then
        assertNotNull(result);
        assertEquals(expectedOption.getPrice(), result.getPrice());
        verify(productServiceClient, times(1)).getProductOption(productId, optionId);
    }

    @Test
    @DisplayName("상품 옵션 조회 테스트 - 실패")
    void getProductOptionTest_Failure() {
        // given
        Long productId = 1L;
        Long optionId = 1L;

        given(productServiceClient.getProductOption(productId, optionId)).willThrow(new CustomException("상품 옵션 조회 실패"));

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            productServiceClient.getProductOption(productId, optionId);
        });

        assertEquals("상품 옵션 조회 실패", exception.getMessage());
        verify(productServiceClient, times(1)).getProductOption(productId, optionId);
    }
}
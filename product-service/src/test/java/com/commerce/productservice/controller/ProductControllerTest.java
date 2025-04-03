package com.commerce.productservice.controller;

import com.commerce.common.dto.ResponseDto;
import com.commerce.productservice.dto.ProductDetailDto;
import com.commerce.productservice.dto.ProductDetailDto.ProductOptionDto;
import com.commerce.productservice.dto.ProductListDto;
import com.commerce.productservice.dto.ProductOptionDetailDto;
import com.commerce.productservice.dto.ProductPriceDto;
import com.commerce.productservice.dto.ProductSaveDto;
import com.commerce.productservice.dto.ProductSearchDto;
import com.commerce.productservice.entity.Product.ProductCategory;
import com.commerce.productservice.entity.Product.ProductStatus;
import com.commerce.productservice.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    @Test
    @DisplayName("상품저장 API 테스트")
    void saveProductTest() {
        // given
        ProductSaveDto productSaveDto = new ProductSaveDto();
        productSaveDto.setTitle("테스트 상품");
        productSaveDto.setContent("테스트 상품 설명");
        productSaveDto.setImageLob("이미지데이터");
        productSaveDto.setStatus(ProductStatus.AVAILABLE);
        productSaveDto.setPrice(10000);
        productSaveDto.setCategory(ProductCategory.LIVING);
        productSaveDto.setStartDate(LocalDateTime.now());

        doNothing().when(productService).saveProduct(any(ProductSaveDto.class));

        // when
        ResponseDto<?> response = productController.saveProduct(productSaveDto);

        // then
        assertEquals(200, response.getCode());
        verify(productService).saveProduct(productSaveDto);
    }

    @Test
    @DisplayName("상품 목록조회 API 테스트")
    void findProductListTest() {
        // given
        ProductSearchDto searchDto = ProductSearchDto.builder()
            .title("테스트")
            .build();

        Pageable pageable = PageRequest.of(0, 10);

        List<ProductListDto> productList = new ArrayList<>();
        ProductListDto product = new ProductListDto();
        product.setId(1L);
        product.setTitle("테스트 상품");
        product.setPrice(10000);
        product.setImageId(1L);
        product.setImageLob("이미지데이터");
        product.setStatus(ProductStatus.AVAILABLE);
        product.setCategory(ProductCategory.LIVING);
        product.setContent("테스트 상품 설명");
        product.setWishlistCount(5);
        product.setIsWishlisted(false);
        productList.add(product);

        Page<ProductListDto> productPage = new PageImpl<>(productList, pageable, 1);

        given(productService.findProductList(any(ProductSearchDto.class), any(Pageable.class)))
            .willReturn(productPage);

        // when
        ResponseDto<?> response = productController.findProductList(searchDto, pageable);

        // then
        assertEquals(200, response.getCode());
        verify(productService).findProductList(searchDto, pageable);
    }

    @Test
    @DisplayName("상품정보 상세조회 API 테스트")
    void findProductDetailTest() {
        // given
        Long productId = 1L;

        ProductDetailDto productDetail = ProductDetailDto.builder()
            .id(productId)
            .title("테스트 상품")
            .content("테스트 상품 설명")
            .price(10000)
            .category("LIVING")
            .status("AVAILABLE")
            .startDate(LocalDateTime.now())
            .imageLob("이미지데이터")
            .options(new ArrayList<>())
            .build();

        ProductOptionDto option = ProductOptionDto.builder()
            .id(1L)
            .name("옵션1")
            .price(1000)
            .stock(100)
            .build();

        productDetail.getOptions().add(option);

        given(productService.findProductDetail(anyLong())).willReturn(productDetail);

        // when
        ResponseDto<ProductDetailDto> response = productController.findProductDetail(productId);

        // then
        assertEquals(200, response.getCode());
        assertEquals(productId, response.getData().getId());
        verify(productService).findProductDetail(productId);
    }

    @Test
    @DisplayName("상품 ID 유효 검사 API 테스트")
    void validateProductTest() {
        // given
        Long productId = 1L;
        given(productService.validateProduct(anyLong())).willReturn(true);

        // when
        boolean result = productController.validateProduct(productId);

        // then
        assertTrue(result);
        verify(productService).validateProduct(productId);
    }

    @Test
    @DisplayName("상품 이미지 ID 조회 API 테스트")
    void findProductImageIdTest() {
        // given
        Long productId = 1L;
        Long imageId = 1L;
        given(productService.findProductImageId(anyLong())).willReturn(imageId);

        // when
        Long result = productController.findProductImageId(productId);

        // then
        assertEquals(imageId, result);
        verify(productService).findProductImageId(productId);
    }

    @Test
    @DisplayName("상품 가격 조회 API 테스트")
    void getProductPriceTest() {
        // given
        Long productId = 1L;
        ProductPriceDto priceDto = ProductPriceDto.builder()
            .price(10000)
            .build();

        given(productService.getProductPrice(anyLong())).willReturn(priceDto);

        // when
        ProductPriceDto result = productController.getProductPrice(productId);

        // then
        assertEquals(10000, result.getPrice());
        verify(productService).getProductPrice(productId);
    }

    @Test
    @DisplayName("상품 옵션 정보 조회 API 테스트")
    void getProductOptionTest() {
        // given
        Long productId = 1L;
        Long optionId = 1L;

        ProductOptionDto optionDto = ProductOptionDto.builder()
            .id(optionId)
            .name("옵션1")
            .price(1000)
            .stock(100)
            .build();

        given(productService.getProductOption(anyLong(), anyLong())).willReturn(optionDto);

        // when
        ProductOptionDto result = productController.getProductOption(productId, optionId);

        // then
        assertEquals(optionId, result.getId());
        assertEquals("옵션1", result.getName());
        verify(productService).getProductOption(productId, optionId);
    }

    @Test
    @DisplayName("장바구니 service에서 요청하는 상품 상세 정보 API 테스트")
    void findProductInfoTest() {
        // given
        Long productId = 1L;
        Long optionId = 1L;

        ProductOptionDetailDto detailDto = ProductOptionDetailDto.builder()
            .productTitle("테스트 상품")
            .optionName("옵션1")
            .price(10000)
            .imageLob("이미지데이터")
            .build();

        given(productService.findProductInfo(anyLong(), anyLong())).willReturn(detailDto);

        // when
        ProductOptionDetailDto result = productController.findProductInfo(productId, optionId);

        // then
        assertEquals("테스트 상품", result.getProductTitle());
        assertEquals("옵션1", result.getOptionName());
        verify(productService).findProductInfo(productId, optionId);
    }
}
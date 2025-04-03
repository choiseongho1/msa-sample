package com.commerce.productservice.service;

import com.commerce.common.exception.CustomException;
import com.commerce.productservice.dto.ProductDetailDto;
import com.commerce.productservice.dto.ProductDetailDto.ProductOptionDto;
import com.commerce.productservice.dto.ProductListDto;
import com.commerce.productservice.dto.ProductOptionDetailDto;
import com.commerce.productservice.dto.ProductOptionSaveDto;
import com.commerce.productservice.dto.ProductPriceDto;
import com.commerce.productservice.dto.ProductSaveDto;
import com.commerce.productservice.dto.ProductSearchDto;
import com.commerce.productservice.dto.WishlistListDto;
import com.commerce.productservice.entity.Product;
import com.commerce.productservice.entity.Product.ProductCategory;
import com.commerce.productservice.entity.Product.ProductStatus;
import com.commerce.productservice.entity.ProductOption;
import com.commerce.productservice.feign.ImageServiceClient;
import com.commerce.productservice.kafka.ImageEventProducer;
import com.commerce.productservice.kafka.OrderStatusProducer;
import com.commerce.productservice.repository.ProductOptionRepository;
import com.commerce.productservice.repository.ProductRepository;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductOptionRepository productOptionRepository;

    @Mock
    private WishlistService wishlistService;

    @Mock
    private ImageEventProducer imageEventProducer;

    @Mock
    private OrderStatusProducer orderStatusProducer;

    @Mock
    private ImageServiceClient imageServiceClient;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("상품 저장 성공 테스트 - 이미지 있음")
    void saveProduct_WithImage_Test() {
        // given
        ProductSaveDto productSaveDto = createProductSaveDto("테스트 상품", "이미지데이터");
        Product product = createProduct(1L, "테스트 상품");

        when(productRepository.save(any(Product.class))).thenReturn(product);
        doNothing().when(imageEventProducer).sendImageEvent(anyString(), anyLong());

        // when
        productService.saveProduct(productSaveDto);

        // then
        verify(productRepository, times(1)).save(any(Product.class));
        verify(productOptionRepository, times(1)).saveAll(any());
        verify(imageEventProducer, times(1)).sendImageEvent(anyString(), anyLong());
    }

    @Test
    @DisplayName("상품 저장 성공 테스트 - 이미지 없음")
    void saveProduct_WithoutImage_Test() {
        // given
        ProductSaveDto productSaveDto = createProductSaveDto("테스트 상품", null);
        Product product = createProduct(1L, "테스트 상품");

        when(productRepository.save(any(Product.class))).thenReturn(product);

        // when
        productService.saveProduct(productSaveDto);

        // then
        verify(productRepository, times(1)).save(any(Product.class));
        verify(productOptionRepository, times(1)).saveAll(any());
        verify(imageEventProducer, never()).sendImageEvent(anyString(), anyLong());
    }

    @Test
    @DisplayName("상품 저장 성공 테스트 - 이미지 빈 문자열")
    void saveProduct_WithEmptyImage_Test() {
        // given
        ProductSaveDto productSaveDto = createProductSaveDto("테스트 상품", "");
        Product product = createProduct(1L, "테스트 상품");

        when(productRepository.save(any(Product.class))).thenReturn(product);

        // when
        productService.saveProduct(productSaveDto);

        // then
        verify(productRepository, times(1)).save(any(Product.class));
        verify(productOptionRepository, times(1)).saveAll(any());
        verify(imageEventProducer, never()).sendImageEvent(anyString(), anyLong());
    }

    @Test
    @DisplayName("상품 상세 정보 조회 성공 테스트 - 이미지 있음")
    void findProductDetail_WithImage_Test() {
        // given
        Long productId = 1L;
        Long imageId = 1L;
        String imageLob = "이미지데이터";

        Product product = createProductWithOptions(productId, "테스트 상품", imageId);

        given(productRepository.findById(anyLong())).willReturn(Optional.of(product));
        given(imageServiceClient.getImageLob(anyLong())).willReturn(imageLob);

        // when
        ProductDetailDto result = productService.findProductDetail(productId);

        // then
        assertNotNull(result);
        assertEquals(productId, result.getId());
        assertEquals("테스트 상품", result.getTitle());
        assertEquals(imageLob, result.getImageLob());
        assertEquals(1, result.getOptions().size());
        verify(productRepository).findById(productId);
        verify(imageServiceClient).getImageLob(imageId);
    }

    @Test
    @DisplayName("상품 상세 정보 조회 성공 테스트 - 이미지 없음")
    void findProductDetail_WithoutImage_Test() {
        // given
        Long productId = 1L;
        Product product = createProductWithOptions(productId, "테스트 상품", null);

        given(productRepository.findById(anyLong())).willReturn(Optional.of(product));

        // when
        ProductDetailDto result = productService.findProductDetail(productId);

        // then
        assertNotNull(result);
        assertEquals(productId, result.getId());
        assertEquals("테스트 상품", result.getTitle());
        assertNull(result.getImageLob());
        assertEquals(1, result.getOptions().size());
        verify(productRepository).findById(productId);
        verify(imageServiceClient, never()).getImageLob(anyLong());
    }

    @Test
    @DisplayName("상품 상세 정보 조회 실패 테스트 - 상품 없음")
    void findProductDetail_ProductNotFound_Test() {
        // given
        Long productId = 1L;
        given(productRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThrows(CustomException.class, () -> productService.findProductDetail(productId));
        verify(productRepository).findById(productId);
    }

    @Test
    @DisplayName("상품 상세 정보 조회 실패 테스트 - 이미지 서비스 오류")
    void findProductDetail_ImageServiceError_Test() {
        // given
        Long productId = 1L;
        Long imageId = 1L;
        Product product = createProductWithOptions(productId, "테스트 상품", imageId);

        given(productRepository.findById(anyLong())).willReturn(Optional.of(product));
        given(imageServiceClient.getImageLob(anyLong())).willThrow(
            new RuntimeException("이미지 서비스 오류"));

        // when & then
        assertThrows(RuntimeException.class, () -> productService.findProductDetail(productId));
        verify(productRepository).findById(productId);
        verify(imageServiceClient).getImageLob(imageId);
    }

    @Test
    @DisplayName("Fallback 메소드 테스트")
    void fallbackImage_Test() {
        // given
        Long productId = 1L;
        Product product = createProductWithOptions(productId, "테스트 상품", 1L);
        Throwable throwable = new RuntimeException("이미지 서비스 오류");

        given(productRepository.findById(anyLong())).willReturn(Optional.of(product));

        // when
        ProductDetailDto result = productService.fallbackImage(productId, throwable);

        // then
        assertNotNull(result);
        assertEquals(productId, result.getId());
        assertEquals("테스트 상품", result.getTitle());
        assertNull(result.getImageLob());
        verify(productRepository).findById(productId);
    }

    @Test
    @DisplayName("상품 목록 조회 성공 테스트 - 로그인 사용자")
    void findProductList_WithLogin_Test() {
        // given
        ProductSearchDto searchDto = ProductSearchDto.builder()
            .title("테스트")
            .loginId("user123")
            .build();

        Pageable pageable = PageRequest.of(0, 10);

        List<ProductListDto> productList = new ArrayList<>();
        ProductListDto product = createProductListDto(1L, "테스트 상품", 1L);
        productList.add(product);

        Page<ProductListDto> productPage = new PageImpl<>(productList, pageable, 1);

        List<WishlistListDto> wishlistList = new ArrayList<>();
        WishlistListDto wishlist = createWishlistListDto(1L, 1L, "테스트 상품", 10000);
        wishlistList.add(wishlist);

        given(wishlistService.findWishlist(anyString())).willReturn(wishlistList);
        given(productRepository.findProductsList(any(ProductSearchDto.class),
            any(Pageable.class))).willReturn(productPage);
        given(imageServiceClient.getImageLob(anyLong())).willReturn("이미지데이터");

        // when
        Page<ProductListDto> result = productService.findProductList(searchDto, pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("테스트 상품", result.getContent().get(0).getTitle());
        assertTrue(result.getContent().get(0).getIsWishlisted());
        assertEquals("이미지데이터", result.getContent().get(0).getImageLob());
        verify(wishlistService).findWishlist(searchDto.getLoginId());
        verify(productRepository).findProductsList(searchDto, pageable);
        verify(imageServiceClient).getImageLob(anyLong());
    }

    @Test
    @DisplayName("상품 목록 조회 성공 테스트 - 비로그인 사용자")
    void findProductList_WithoutLogin_Test() {
        // given
        ProductSearchDto searchDto = ProductSearchDto.builder()
            .title("테스트")
            .loginId(null)
            .build();

        Pageable pageable = PageRequest.of(0, 10);

        List<ProductListDto> productList = new ArrayList<>();
        ProductListDto product = createProductListDto(1L, "테스트 상품", 1L);
        productList.add(product);

        Page<ProductListDto> productPage = new PageImpl<>(productList, pageable, 1);

        given(productRepository.findProductsList(any(ProductSearchDto.class),
            any(Pageable.class))).willReturn(productPage);
        given(imageServiceClient.getImageLob(anyLong())).willReturn("이미지데이터");

        // when
        Page<ProductListDto> result = productService.findProductList(searchDto, pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("테스트 상품", result.getContent().get(0).getTitle());
        assertFalse(result.getContent().get(0).getIsWishlisted());
        assertEquals("이미지데이터", result.getContent().get(0).getImageLob());
        verify(wishlistService, never()).findWishlist(anyString());
        verify(productRepository).findProductsList(searchDto, pageable);
        verify(imageServiceClient).getImageLob(anyLong());
    }

    @Test
    @DisplayName("상품 목록 조회 테스트 - 이미지 조회 실패")
    void findProductList_ImageError_Test() {
        // given
        ProductSearchDto searchDto = ProductSearchDto.builder()
            .title("테스트")
            .loginId("user123")
            .build();

        Pageable pageable = PageRequest.of(0, 10);

        List<ProductListDto> productList = new ArrayList<>();
        ProductListDto product = createProductListDto(1L, "테스트 상품", 1L);
        productList.add(product);

        Page<ProductListDto> productPage = new PageImpl<>(productList, pageable, 1);

        List<WishlistListDto> wishlistList = new ArrayList<>();
        WishlistListDto wishlist = createWishlistListDto(1L, 1L, "테스트 상품", 10000);
        wishlistList.add(wishlist);

        given(wishlistService.findWishlist(anyString())).willReturn(wishlistList);
        given(productRepository.findProductsList(any(ProductSearchDto.class),
            any(Pageable.class))).willReturn(productPage);
        given(imageServiceClient.getImageLob(anyLong())).willThrow(
            new RuntimeException("이미지 서비스 오류"));

        // when
        Page<ProductListDto> result = productService.findProductList(searchDto, pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("테스트 상품", result.getContent().get(0).getTitle());
        assertTrue(result.getContent().get(0).getIsWishlisted());
        assertNull(result.getContent().get(0).getImageLob());
        verify(wishlistService).findWishlist(searchDto.getLoginId());
        verify(productRepository).findProductsList(searchDto, pageable);
        verify(imageServiceClient).getImageLob(anyLong());
    }

    @Test
    @DisplayName("상품 목록 조회 테스트 - 이미지 ID가 null인 경우")
    void findProductList_NullImageId_Test() {
        // given
        ProductSearchDto searchDto = ProductSearchDto.builder()
            .title("테스트")
            .loginId("user123")
            .build();

        Pageable pageable = PageRequest.of(0, 10);

        List<ProductListDto> productList = new ArrayList<>();
        ProductListDto product = createProductListDto(1L, "테스트 상품", null);
        productList.add(product);

        Page<ProductListDto> productPage = new PageImpl<>(productList, pageable, 1);

        List<WishlistListDto> wishlistList = new ArrayList<>();
        WishlistListDto wishlist = createWishlistListDto(1L, 1L, "테스트 상품", 10000);
        wishlistList.add(wishlist);

        given(wishlistService.findWishlist(anyString())).willReturn(wishlistList);
        given(productRepository.findProductsList(any(ProductSearchDto.class),
            any(Pageable.class))).willReturn(productPage);

        // when
        Page<ProductListDto> result = productService.findProductList(searchDto, pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("테스트 상품", result.getContent().get(0).getTitle());
        assertTrue(result.getContent().get(0).getIsWishlisted());
        assertNull(result.getContent().get(0).getImageLob());
        verify(wishlistService).findWishlist(searchDto.getLoginId());
        verify(productRepository).findProductsList(searchDto, pageable);
        verify(imageServiceClient, never()).getImageLob(anyLong());
    }

    @Test
    @DisplayName("상품 재고 감소 성공 테스트")
    void decreaseStock_Success_Test() {
        // given
        Long productId = 1L;
        Long optionId = 1L;
        Integer quantity = 5;
        Long orderId = 1L;

        ProductOption option = ProductOption.builder()
            .id(optionId)
            .name("옵션1")
            .price(1000)
            .stock(10)
            .build();

        given(productOptionRepository.findById(anyLong())).willReturn(Optional.of(option));

        // when
        productService.decreaseStock(productId, optionId, quantity, orderId);

        // then
        assertEquals(5, option.getStock());
        verify(productOptionRepository).findById(optionId);
        verify(productOptionRepository).save(option);
    }

    @Test
    @DisplayName("상품 재고 감소 실패 테스트 - 재고 부족")
    void decreaseStock_OutOfStock_Test() {
        // given
        Long productId = 1L;
        Long optionId = 1L;
        Integer quantity = 15;
        Long orderId = 1L;

        ProductOption option = ProductOption.builder()
            .id(optionId)
            .name("옵션1")
            .price(1000)
            .stock(10)
            .build();

        given(productOptionRepository.findById(anyLong())).willReturn(Optional.of(option));
        doNothing().when(orderStatusProducer).sendOrderStatusUpdateEvent(anyLong(), anyString());

        // when
        productService.decreaseStock(productId, optionId, quantity, orderId);

        // then
        assertEquals(10, option.getStock()); // 재고 변경 없음
        verify(productOptionRepository).findById(optionId);
        verify(orderStatusProducer).sendOrderStatusUpdateEvent(orderId, "OUT_OF_STOCK");
    }

    @Test
    @DisplayName("상품 재고 감소 테스트 - 옵션 ID가 null인 경우")
    void decreaseStock_NullOptionId_Test() {
        // given
        Long productId = 1L;
        Long optionId = null;
        Integer quantity = 5;
        Long orderId = 1L;

        // when
        productService.decreaseStock(productId, optionId, quantity, orderId);

        // then
        verify(productOptionRepository, never()).findById(anyLong());
        verify(productOptionRepository, never()).save(any(ProductOption.class));
        verify(orderStatusProducer, never()).sendOrderStatusUpdateEvent(anyLong(), anyString());
    }

    @Test
    @DisplayName("상품 재고 감소 실패 테스트 - 옵션 없음")
    void decreaseStock_OptionNotFound_Test() {
        // given
        Long productId = 1L;
        Long optionId = 1L;
        Integer quantity = 5;
        Long orderId = 1L;

        given(productOptionRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThrows(CustomException.class,
            () -> productService.decreaseStock(productId, optionId, quantity, orderId));
        verify(productOptionRepository).findById(optionId);
        verify(productOptionRepository, never()).save(any(ProductOption.class));
        verify(orderStatusProducer, never()).sendOrderStatusUpdateEvent(anyLong(), anyString());
    }

    @Test
    @DisplayName("상품 유효성 검증 성공 테스트")
    void validateProduct_Success_Test() {
        // given
        Long productId = 1L;
        given(productRepository.existsById(anyLong())).willReturn(true);

        // when
        boolean result = productService.validateProduct(productId);

        // then
        assertTrue(result);
        verify(productRepository).existsById(productId);
    }

    @Test
    @DisplayName("상품 유효성 검증 실패 테스트")
    void validateProduct_NotExists_Test() {
        // given
        Long productId = 1L;
        given(productRepository.existsById(anyLong())).willReturn(false);

        // when
        boolean result = productService.validateProduct(productId);

        // then
        assertFalse(result);
        verify(productRepository).existsById(productId);
    }

    @Test
    @DisplayName("상품 이미지 ID 조회 성공 테스트")
    void findProductImageId_Success_Test() {
        // given
        Long productId = 1L;
        Long imageId = 1L;

        Product product = Product.builder()
            .id(productId)
            .imageId(imageId)
            .build();

        given(productRepository.findById(anyLong())).willReturn(Optional.of(product));

        // when
        Long result = productService.findProductImageId(productId);

        // then
        assertEquals(imageId, result);
        verify(productRepository).findById(productId);
    }

    @Test
    @DisplayName("상품 이미지 ID 조회 실패 테스트 - 상품 없음")
    void findProductImageId_ProductNotFound_Test() {
        // given
        Long productId = 1L;
        given(productRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThrows(Exception.class, () -> productService.findProductImageId(productId));
        verify(productRepository).findById(productId);
    }

    @Test
    @DisplayName("상품 가격 조회 성공 테스트")
    void getProductPrice_Success_Test() {
        // given
        Long productId = 1L;
        Integer price = 10000;

        Product product = Product.builder()
            .id(productId)
            .price(price)
            .build();

        given(productRepository.findById(anyLong())).willReturn(Optional.of(product));

        // when
        ProductPriceDto result = productService.getProductPrice(productId);

        // then
        assertEquals(price, result.getPrice());
        verify(productRepository).findById(productId);
    }

    @Test
    @DisplayName("상품 가격 조회 실패 테스트 - 상품 없음")
    void getProductPrice_ProductNotFound_Test() {
        // given
        Long productId = 1L;
        given(productRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThrows(CustomException.class, () -> productService.getProductPrice(productId));
        verify(productRepository).findById(productId);
    }

    @Test
    @DisplayName("상품 옵션 정보 조회 성공 테스트")
    void getProductOption_Success_Test() {
        // given
        Long productId = 1L;
        Long optionId = 1L;

        Product product = Product.builder()
            .id(productId)
            .build();

        ProductOption option = ProductOption.builder()
            .id(optionId)
            .name("옵션1")
            .price(1000)
            .stock(100)
            .product(product)
            .build();

        given(productRepository.findById(anyLong())).willReturn(Optional.of(product));
        given(productOptionRepository.findById(anyLong())).willReturn(Optional.of(option));

        // when
        ProductOptionDto result = productService.getProductOption(productId, optionId);

        // then
        assertEquals(optionId, result.getId());
        assertEquals("옵션1", result.getName());
        assertEquals(1000, result.getPrice());
        assertEquals(100, result.getStock());
        verify(productRepository).findById(productId);
        verify(productOptionRepository).findById(optionId);
    }

    @Test
    @DisplayName("상품 옵션 정보 조회 실패 테스트 - 상품 없음")
    void getProductOption_ProductNotFound_Test() {
        // given
        Long productId = 1L;
        Long optionId = 1L;
        given(productRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThrows(CustomException.class,
            () -> productService.getProductOption(productId, optionId));
        verify(productRepository).findById(productId);
        verify(productOptionRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("상품 옵션 정보 조회 실패 테스트 - 옵션 없음")
    void getProductOption_OptionNotFound_Test() {
        // given
        Long productId = 1L;
        Long optionId = 1L;

        Product product = Product.builder()
            .id(productId)
            .build();

        given(productRepository.findById(anyLong())).willReturn(Optional.of(product));
        given(productOptionRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThrows(CustomException.class,
            () -> productService.getProductOption(productId, optionId));
        verify(productRepository).findById(productId);
        verify(productOptionRepository).findById(optionId);
    }

    @Test
    @DisplayName("상품 옵션 정보 조회 실패 테스트 - 상품과 옵션 불일치")
    void getProductOption_MismatchProductOption_Test() {
        // given
        Long productId = 1L;
        Long optionId = 1L;
        Long wrongProductId = 2L;

        Product product = Product.builder()
            .id(productId)
            .build();

        Product wrongProduct = Product.builder()
            .id(wrongProductId)
            .build();

        ProductOption option = ProductOption.builder()
            .id(optionId)
            .name("옵션1")
            .price(1000)
            .stock(100)
            .product(wrongProduct)
            .build();

        given(productRepository.findById(anyLong())).willReturn(Optional.of(product));
        given(productOptionRepository.findById(anyLong())).willReturn(Optional.of(option));

        // when & then
        assertThrows(CustomException.class,
            () -> productService.getProductOption(productId, optionId));
        verify(productRepository).findById(productId);
        verify(productOptionRepository).findById(optionId);
    }

    @Test
    @DisplayName("상품 상세 정보 조회 성공 테스트 (장바구니용) - 이미지 있음")
    void findProductInfo_WithImage_Test() {
        // given
        Long productId = 1L;
        Long optionId = 1L;
        Long imageId = 1L;
        String imageLob = "이미지데이터";

        Product product = Product.builder()
            .id(productId)
            .title("테스트 상품")
            .imageId(imageId)
            .build();

        ProductOption option = ProductOption.builder()
            .id(optionId)
            .name("옵션1")
            .price(1000)
            .product(product)
            .build();

        given(productRepository.findById(anyLong())).willReturn(Optional.of(product));
        given(productOptionRepository.findById(anyLong())).willReturn(Optional.of(option));
        given(imageServiceClient.getImageLob(anyLong())).willReturn(imageLob);

        // when
        ProductOptionDetailDto result = productService.findProductInfo(productId, optionId);

        // then
        assertEquals("테스트 상품", result.getProductTitle());
        assertEquals("옵션1", result.getOptionName());
        assertEquals(1000, result.getPrice());
        assertEquals(imageLob, result.getImageLob());
        verify(productRepository).findById(productId);
        verify(productOptionRepository).findById(optionId);
        verify(imageServiceClient).getImageLob(imageId);
    }


    @Test
    @DisplayName("상품 상세 정보 조회 성공 테스트 (장바구니용) - 이미지 없음")
    void findProductInfo_WithoutImage_Test() {
        // given
        Long productId = 1L;
        Long optionId = 1L;

        Product product = Product.builder()
            .id(productId)
            .title("테스트 상품")
            .imageId(null)
            .build();

        ProductOption option = ProductOption.builder()
            .id(optionId)
            .name("옵션1")
            .price(1000)
            .product(product)
            .build();

        given(productRepository.findById(anyLong())).willReturn(Optional.of(product));
        given(productOptionRepository.findById(anyLong())).willReturn(Optional.of(option));

        // when
        ProductOptionDetailDto result = productService.findProductInfo(productId, optionId);

        // then
        assertEquals("테스트 상품", result.getProductTitle());
        assertEquals("옵션1", result.getOptionName());
        assertEquals(1000, result.getPrice());
        assertNull(result.getImageLob());
        verify(productRepository).findById(productId);
        verify(productOptionRepository).findById(optionId);
        verify(imageServiceClient, never()).getImageLob(anyLong());
    }

    @Test
    @DisplayName("상품 상세 정보 조회 실패 테스트 (장바구니용) - 상품 없음")
    void findProductInfo_ProductNotFound_Test() {
        // given
        Long productId = 1L;
        Long optionId = 1L;
        given(productRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThrows(CustomException.class, () -> productService.findProductInfo(productId, optionId));
        verify(productRepository).findById(productId);
        verify(productOptionRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("상품 상세 정보 조회 실패 테스트 (장바구니용) - 옵션 없음")
    void findProductInfo_OptionNotFound_Test() {
        // given
        Long productId = 1L;
        Long optionId = 1L;

        Product product = Product.builder()
            .id(productId)
            .title("테스트 상품")
            .build();

        given(productRepository.findById(anyLong())).willReturn(Optional.of(product));
        given(productOptionRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThrows(CustomException.class, () -> productService.findProductInfo(productId, optionId));
        verify(productRepository).findById(productId);
        verify(productOptionRepository).findById(optionId);
    }

    // 헬퍼 메소드
    private ProductSaveDto createProductSaveDto(String title, String imageLob) {
        ProductSaveDto productSaveDto = new ProductSaveDto();
        productSaveDto.setTitle(title);
        productSaveDto.setContent("테스트 상품 설명");
        productSaveDto.setImageLob(imageLob);
        productSaveDto.setStatus(ProductStatus.AVAILABLE);
        productSaveDto.setPrice(10000);
        productSaveDto.setCategory(ProductCategory.LIVING);
        productSaveDto.setStartDate(LocalDateTime.now());

        List<ProductOptionSaveDto> options = new ArrayList<>();
        ProductOptionSaveDto option = new ProductOptionSaveDto();
        option.setName("옵션1");
        option.setPrice(1000);
        option.setStock(100);
        options.add(option);

        productSaveDto.setOptions(options);

        return productSaveDto;
    }

    private Product createProduct(Long id, String title) {
        return Product.builder()
            .id(id)
            .title(title)
            .content("테스트 상품 설명")
            .status(ProductStatus.AVAILABLE)
            .price(10000)
            .category(ProductCategory.LIVING)
            .build();
    }

    private Product createProductWithOptions(Long id, String title, Long imageId) {
        Product product = Product.builder()
            .id(id)
            .title(title)
            .content("테스트 상품 설명")
            .status(ProductStatus.AVAILABLE)
            .price(10000)
            .category(ProductCategory.LIVING)
            .imageId(imageId)
            .productOptions(new ArrayList<>())
            .build();

        ProductOption option = ProductOption.builder()
            .id(1L)
            .name("옵션1")
            .price(1000)
            .stock(100)
            .product(product)
            .build();

        product.getProductOptions().add(option);

        return product;
    }

    private ProductListDto createProductListDto(Long id, String title, Long imageId) {
        ProductListDto product = new ProductListDto();
        product.setId(id);
        product.setTitle(title);
        product.setPrice(10000);
        product.setImageId(imageId);
        product.setStatus(ProductStatus.AVAILABLE);
        product.setCategory(ProductCategory.LIVING);
        return product;
    }

    private WishlistListDto createWishlistListDto(Long wishlistId, Long productId, String productTitle, Integer price) {
        WishlistListDto wishlist = new WishlistListDto();
        wishlist.setWishlistId(wishlistId);
        wishlist.setProductId(productId);
        wishlist.setProductTitle(productTitle);
        wishlist.setPrice(price);
        return wishlist;
    }

}

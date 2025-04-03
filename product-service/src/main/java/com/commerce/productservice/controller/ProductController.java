package com.commerce.productservice.controller;

import com.commerce.common.dto.ResponseDto;
import com.commerce.productservice.dto.ProductDetailDto;
import com.commerce.productservice.dto.ProductDetailDto.ProductOptionDto;
import com.commerce.productservice.dto.ProductListDto;
import com.commerce.productservice.dto.ProductOptionDetailDto;
import com.commerce.productservice.dto.ProductPriceDto;
import com.commerce.productservice.dto.ProductSaveDto;
import com.commerce.productservice.dto.ProductSearchDto;
import com.commerce.productservice.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import reactor.core.publisher.Mono;

/**
 * Product-service의 Controller
 *
 * @author : choiseongho
 * @fileName : ProductController.java
 * @since : 2025-01-03
 */

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;


    /**
     * 상품저장 API
     *
     * @param productSaveDto the product save dto
     */
    @PostMapping
    @Operation(summary = "상품 등록")
    public ResponseDto<?> saveProduct(@Valid @RequestBody ProductSaveDto productSaveDto) {
        productService.saveProduct(productSaveDto);
        return ResponseDto.ok();
    }

    /**
     * 상품 목록조회 API
     *
     * @param searchDto the search dto
     * @param pageable  the pageable
     */
    @GetMapping
    @Operation(summary = "상품 목록 조회")
    public ResponseDto<?> findProductList(@Valid @ParameterObject ProductSearchDto searchDto,
        @Parameter(hidden = true) Pageable pageable) {
        Page<ProductListDto> productList = productService.findProductList(searchDto, pageable);

        return ResponseDto.ok(productList);
    }

    /**
     * 상품정보 상세조회 API
     *
     * @param productId the product id
     */
    @GetMapping("/{productId}")
    @Operation(summary = "상품 정보 상세 조회")
    public ResponseDto<ProductDetailDto> findProductDetail(@PathVariable(name = "productId") Long productId) {
        ProductDetailDto productDetail = productService.findProductDetail(productId);

        return ResponseDto.ok(productDetail);
    }


    // ------------------------------------
    // Feign Client 영역
    // ------------------------------------


    /**
     * 상품 ID 유효 검사 API
     *
     * @param id the id
     */

    @GetMapping("/validate/{id}")
    @Operation(summary = "[Feign] 상품 ID 유효 검사")
    public boolean validateProduct(@PathVariable(name = "id") Long id) {
        return productService.validateProduct(id);
    }

    /**
     * 상품 이미지 ID 조회 API
     *
     * @param productId the product id
     */
    @GetMapping("/{productId}/image")
    @Operation(summary = "[Feign] 상품 이미지 ID 조회")
    public Long findProductImageId(@PathVariable(name = "productId") Long productId) {
        return productService.findProductImageId(productId);
    }

    /**
     * 상품 가격 조회 API
     *
     * @param productId the product id
     */
    @GetMapping("/{productId}/price")
    @Operation(summary = "[Feign] 상품 가격 조회")
    public ProductPriceDto getProductPrice(@PathVariable(name = "productId") Long productId) {
        return productService.getProductPrice(productId);
    }

    /**
     * 상품 옵션 정보 조회 API
     *
     * @param productId 상품 ID
     * @param optionId 옵션 ID
     */
    @GetMapping("/{productId}/option/{optionId}")
    @Operation(summary = "[Feign] 상품 옵션 정보 조회")
    public ProductOptionDto getProductOption(@PathVariable(name = "productId") Long productId, @PathVariable(name = "optionId") Long optionId) {
        return productService.getProductOption(productId, optionId);
    }

    /**
     * 장바구니 service에서 요청하는 상품 상세 정보 API
     *
     * @param productId the product id
     * @param optionId  the option id
     */

    @GetMapping("/{productId}/{optionId}")
    @Operation(summary = "[Feign] 상품 상세 정보")
    public ProductOptionDetailDto findProductInfo(@PathVariable(name = "productId") Long productId, @PathVariable(name = "optionId") Long optionId) {
        return productService.findProductInfo(productId, optionId);
    }

}

package com.commerce.orderservice.feign;

import com.commerce.orderservice.config.FeignConfig;
import com.commerce.orderservice.dto.ProductOptionDto;
import com.commerce.orderservice.dto.ProductPriceDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


/**
 * Order-Service에서 타 서비스로 feign client를 요청하기 위한 클래스
 *
 * @author : choiseongho
 * @fileName : OrderFeignClient.java
 * @since : 2025-01-09
 */
@FeignClient(name = "product-service", url="${api.apigateway}/product-service", configuration = FeignConfig.class)
public interface ProductServiceClient {
    @GetMapping("/product/validate/{productId}")
    boolean validateProduct(@PathVariable("productId") Long productId);

    @GetMapping("/product/{productId}/image")
    Long findProductImageId(@PathVariable("productId") Long productId);

    @GetMapping("/product/{productId}/price")
    ProductPriceDto getProductPrice(@PathVariable("productId") Long productId);

    @GetMapping("/product/{productId}/option/{optionId}")
    ProductOptionDto getProductOption(@PathVariable("productId") Long productId, @PathVariable("optionId") Long optionId);
}
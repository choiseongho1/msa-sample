package com.commerce.cartservice.feign;

import com.commerce.cartservice.config.FeignConfig;
import com.commerce.cartservice.dto.ProductOptionDetailDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", url = "${api.apigateway}/product-service",  configuration = FeignConfig.class)
public interface ProductServiceClient {

    @GetMapping("/product/{productId}/{optionId}")
    ProductOptionDetailDto findProductInfo(@PathVariable("productId") Long productId, @PathVariable("optionId") Long optionId);
}
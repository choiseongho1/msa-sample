package com.commerce.productservice.feign;

import com.commerce.productservice.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.retry.annotation.CircuitBreaker;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "image-service", url = "${api.apigateway}/image-service",  configuration = FeignConfig.class)
public interface ImageServiceClient {

    @GetMapping("/image/{imageId}")
    String getImageLob(@PathVariable("imageId") Long imageId);
}
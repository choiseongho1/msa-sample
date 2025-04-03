package com.commerce.apigatewayservice.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @LoadBalanced  // 서비스 디스커버리를 위한 로드밸런서 활성화
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
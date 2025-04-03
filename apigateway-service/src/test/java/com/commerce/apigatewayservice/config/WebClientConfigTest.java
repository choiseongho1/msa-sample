package com.commerce.apigatewayservice.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class WebClientConfigTest {

    @Autowired
    private ApplicationContext context;

    @Test
    @DisplayName("WebClient.Builder 빈이 정상적으로 등록되는지 확인")
    void testWebClientBuilderBeanRegistration() {
        // Given & When
        WebClient.Builder webClientBuilder = context.getBean(WebClient.Builder.class);

        // Then
        assertThat(webClientBuilder).isNotNull();
    }

    @Test
    @DisplayName("WebClientConfig 빈이 정상적으로 등록되는지 확인")
    void testWebClientConfigBeanRegistration() {
        // Given & When
        WebClientConfig webClientConfig = context.getBean(WebClientConfig.class);

        // Then
        assertThat(webClientConfig).isNotNull();
    }
}
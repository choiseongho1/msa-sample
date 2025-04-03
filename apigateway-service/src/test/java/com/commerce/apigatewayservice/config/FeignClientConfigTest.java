package com.commerce.apigatewayservice.config;

import feign.RequestInterceptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
class FeignClientConfigTest {

    @Autowired
    private ApplicationContext context;

    @Test
    @DisplayName("RequestInterceptor 빈이 정상적으로 등록되는지 확인")
    void testRequestInterceptorBeanRegistration() {
        // Given & When
        RequestInterceptor requestInterceptor = context.getBean(RequestInterceptor.class);

        // Then
        assertThat(requestInterceptor).isNotNull();
    }

    @Test
    @DisplayName("FeignClientConfig 빈이 정상적으로 등록되는지 확인")
    void testFeignClientConfigBeanRegistration() {
        // Given & When
        FeignClientConfig feignClientConfig = context.getBean(FeignClientConfig.class);

        // Then
        assertThat(feignClientConfig).isNotNull();
    }
}
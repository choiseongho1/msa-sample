package com.commerce.apigatewayservice.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SecurityConfigTest {

    @Autowired
    private ApplicationContext context;

    @Test
    @DisplayName("SecurityWebFilterChain 빈이 정상적으로 등록되는지 확인")
    void testSecurityWebFilterChainBeanRegistration() {
        // Given & When
        SecurityWebFilterChain filterChain = context.getBean(SecurityWebFilterChain.class);

        // Then
        assertThat(filterChain).isNotNull();
    }

    @Test
    @DisplayName("SecurityConfig 빈이 정상적으로 등록되는지 확인")
    void testSecurityConfigBeanRegistration() {
        // Given & When
        SecurityConfig securityConfig = context.getBean(SecurityConfig.class);

        // Then
        assertThat(securityConfig).isNotNull();
    }
}
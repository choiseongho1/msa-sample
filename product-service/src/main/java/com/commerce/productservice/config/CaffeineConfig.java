package com.commerce.productservice.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine Cache 설정 파일
 */
@Configuration
@EnableCaching
public class CaffeineConfig {

    @Bean
    public Caffeine<Object, Object> caffeineBuilder() {
        return Caffeine.newBuilder()
            .maximumSize(1000) // Cache up to 1000 entries
            .expireAfterWrite(1, TimeUnit.HOURS); // Expire after 1 hour
    }

    @Bean
    @Primary
    public CacheManager cacheManager(Caffeine<Object, Object> caffeineBuilder) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("wishlist");
        cacheManager.setCaffeine(caffeineBuilder);
        return cacheManager;
    }
}

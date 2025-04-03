package com.commerce.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 기능을 활성화하기 위한 설정 클래스
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
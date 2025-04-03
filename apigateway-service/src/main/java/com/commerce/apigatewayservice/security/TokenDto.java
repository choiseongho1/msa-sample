package com.commerce.apigatewayservice.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Token 관련 DTO
 *
 * @author : choiseongho
 * @fileName : Token.java
 * @since : 2025-01-02
 */

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenDto {

    private String accessToken;
    private String refreshToken;
}

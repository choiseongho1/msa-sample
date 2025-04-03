package com.commerce.common.dto;

import lombok.*;

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

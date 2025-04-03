package com.commerce.userservice.security;

import com.commerce.common.dto.TokenDto;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * JWT 관련 Util
 *
 * @author : choiseongho
 * @fileName : JwtTokenUtil.java
 * @since : 2025-01-02
 */
@Component
@Slf4j
public class JwtTokenUtil {


    private final RedisTemplate<String, String> redisTemplate;

    public JwtTokenUtil(@Qualifier("redisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private final long accessTokenValidTime = 1000 * 60 * 30; // 30분
    private final long refreshTokenValidTime = 1000 * 60 * 60 * 24 * 7; // 7일

    @Value("${jwt.token.secretKey}")
    private String secretKey;

    // 토큰 생성 (로그인 시 호출)
    public TokenDto createToken(String userId) {
        String accessToken = generateAccessToken(userId);
        String refreshToken = generateRefreshToken(userId);

        // Redis에 Refresh Token 저장 (key: RT:userId, value: refreshToken)
        redisTemplate.opsForValue()
                .set("RT:" + userId, refreshToken, refreshTokenValidTime, TimeUnit.MILLISECONDS);

        return TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // Access Token 생성
    private String generateAccessToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .claim("userId", userId)
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenValidTime))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    // Refresh Token 생성
    private String generateRefreshToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .claim("userId", userId)
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenValidTime))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    // 토큰 재발급 (Refresh Token으로 새 Access Token 발급)
    public TokenDto refreshToken(String refreshToken) {
        // Refresh Token으로부터 사용자 ID 추출
        String userId = getUserId(refreshToken);
        // Redis에서 저장된 Refresh Token 조회
        String savedRefreshToken = redisTemplate.opsForValue().get("RT:" + userId);

        // Redis에 저장된 Refresh Token과 일치하는지 검증
        if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String newAccessToken = generateAccessToken(userId);
        String newRefreshToken = refreshToken;  // 기본적으로 기존 토큰 유지

        // Refresh Token의 남은 유효기간 확인
        long remainingTime = redisTemplate.getExpire("RT:" + userId, TimeUnit.MILLISECONDS);

        // 남은 기간이 특정 임계값(예: 3일) 이하면 재발급
        if (remainingTime < TimeUnit.DAYS.toMillis(3)) {
            newRefreshToken = generateRefreshToken(userId);
            redisTemplate.opsForValue()
                    .set("RT:" + userId, newRefreshToken, refreshTokenValidTime, TimeUnit.MILLISECONDS);
        }

        return TokenDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    // 토큰에서 userId 추출
    public String getUserId(String token) {
        return extractClaims(token).get("userId").toString();
    }

    // 토큰 만료 여부 확인
    public boolean isExpired(String token) {
        try {
            return extractClaims(token).getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    // 토큰 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    // Claims 추출
    private Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }

    // 로그아웃 처리
    public void logout(String userId) {
        redisTemplate.delete("RT:" + userId);
    }


    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null) {
            return authentication.getName();
        } else {
            throw new RuntimeException("Security Context에 인증 정보가 없습니다.");
        }
    }





}
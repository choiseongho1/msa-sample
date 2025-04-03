package com.commerce.userservice.controller;

import com.commerce.common.dto.ResponseDto;
import com.commerce.common.dto.TokenDto;
import com.commerce.common.exception.CustomException;
import com.commerce.userservice.dto.UserLoginDto;
import com.commerce.userservice.dto.UserSaveDto;
import com.commerce.userservice.security.JwtTokenUtil;
import com.commerce.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * User-service의 Controller
 *
 * @author : choiseongho
 * @fileName : UserController.java
 * @since : 2024-12-31
 */

@Slf4j
@Tag(name = "Auth", description = "인증 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    // User 관련 Service
    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;

    /**
     * 로그인 API
     *
     * @param userLoginDto
     */
    @PostMapping("/login")
    @Operation(summary = "로그인")
    public ResponseDto<?> login(@RequestBody @Valid UserLoginDto userLoginDto) {
        TokenDto token = userService.login(userLoginDto);

        // 응답 헤더에 토큰 추가
        return ResponseDto.ok(token);
    }


    /**
     * JWT 토큰 refresh
     *
     * @param tokenDto
     */
    @PostMapping("/refresh")
    @Operation(summary = "JWT 토큰 refresh")
    public ResponseDto<?> refresh(@RequestBody TokenDto tokenDto) {
        TokenDto tokens = jwtTokenUtil.refreshToken(tokenDto.getRefreshToken());
        return ResponseDto.ok(tokens);
    }

    /**
     * 로그아웃 API
     *
     * @param tokenDto
     */
    @PostMapping("/logout")
    @Operation(summary = "로그아웃")
    public ResponseDto<?> logout(@RequestBody TokenDto tokenDto) {
        try {
            // Bearer 토큰에서 JWT 추출
            String token = tokenDto.getAccessToken();

            // 토큰에서 사용자 ID 추출
            String userId = jwtTokenUtil.getUserId(token);

            // Redis에서 Refresh Token 삭제
            jwtTokenUtil.logout(userId);
            log.info("사용자 : {} --------> redis 에서 refresh token 삭제", userId);
            return ResponseDto.ok();

        } catch (Exception e) {
            log.error("Logout failed: {}", e.getMessage());
            throw new CustomException("예상치 못한 오류가 발생하였습니다.");
        }
    }
}

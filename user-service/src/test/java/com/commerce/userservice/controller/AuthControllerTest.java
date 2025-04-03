package com.commerce.userservice.controller;

import com.commerce.common.dto.ResponseDto;
import com.commerce.common.dto.TokenDto;
import com.commerce.userservice.dto.UserLoginDto;
import com.commerce.userservice.security.JwtTokenUtil;
import com.commerce.userservice.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @InjectMocks
    private AuthController authController;

    @Test
    @DisplayName("로그인 API 테스트")
    void loginTest() {
        // given
        UserLoginDto userLoginDto = UserLoginDto.builder()
            .userId("testuser")
            .userPassword("password123!@#")
            .build();

        TokenDto tokenDto = TokenDto.builder()
            .accessToken("test-access-token")
            .refreshToken("test-refresh-token")
            .build();

        given(userService.login(any(UserLoginDto.class))).willReturn(tokenDto);

        // when
        ResponseDto<?> response = authController.login(userLoginDto);

        // then
        assertEquals(200, response.getCode());
        verify(userService).login(userLoginDto);
    }

    @Test
    @DisplayName("토큰 갱신 API 테스트")
    void refreshTest() {
        // given
        TokenDto requestTokenDto = TokenDto.builder()
            .refreshToken("test-refresh-token")
            .build();

        TokenDto responseTokenDto = TokenDto.builder()
            .accessToken("new-access-token")
            .refreshToken("new-refresh-token")
            .build();

        given(jwtTokenUtil.refreshToken(anyString())).willReturn(responseTokenDto);

        // when
        ResponseDto<?> response = authController.refresh(requestTokenDto);

        // then
        assertEquals(200, response.getCode());
        verify(jwtTokenUtil).refreshToken(requestTokenDto.getRefreshToken());
    }

    @Test
    @DisplayName("로그아웃 API 테스트")
    void logoutTest() {
        // given
        TokenDto tokenDto = TokenDto.builder()
            .accessToken("test-access-token")
            .build();

        given(jwtTokenUtil.getUserId(anyString())).willReturn("testuser");
        doNothing().when(jwtTokenUtil).logout(anyString());

        // when
        ResponseDto<?> response = authController.logout(tokenDto);

        // then
        assertEquals(200, response.getCode());
        verify(jwtTokenUtil).getUserId(tokenDto.getAccessToken());
        verify(jwtTokenUtil).logout("testuser");
    }
}
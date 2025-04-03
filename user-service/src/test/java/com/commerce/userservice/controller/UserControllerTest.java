package com.commerce.userservice.controller;

import com.commerce.common.dto.ResponseDto;
import com.commerce.userservice.dto.UserDetailDto;
import com.commerce.userservice.dto.UserSaveDto;
import com.commerce.userservice.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    @DisplayName("회원가입 API 테스트")
    void signupTest() {
        // given
        UserSaveDto userSaveDto = UserSaveDto.builder()
            .userId("testuser")
            .userPassword("password123!@#")
            .userEmail("test@example.com")
            .userName("Test User")
            .userAddress("Test Address")
            .userZipcode("12345")
            .userPhone("01012345678")
            .build();

        doNothing().when(userService).createUser(any(UserSaveDto.class));

        // when
        ResponseDto<?> response = userController.signup(userSaveDto);

        // then
        assertEquals(200, response.getCode());
        verify(userService).createUser(userSaveDto);
    }

    @Test
    @DisplayName("사용자 정보 조회 API 테스트")
    void getLoginUserByUserIdTest() {
        // given
        String userId = "testuser";
        UserDetailDto userDetailDto = UserDetailDto.builder()
            .userId(userId)
            .userEmail("test@example.com")
            .userName("Test User")
            .userAddress("Test Address")
            .userZipcode("12345")
            .userPhone("01012345678")
            .build();

        given(userService.getLoginUserByUserId(anyString())).willReturn(userDetailDto);

        // when
        ResponseDto<?> response = userController.getLoginUserByUserId(userId);

        // then
        assertEquals(200, response.getCode());
        verify(userService).getLoginUserByUserId(userId);
    }

    @Test
    @DisplayName("사용자 유효성 검증 API 테스트")
    void validateUserTest() {
        // given
        String userId = "testuser";
        given(userService.validateUser(anyString())).willReturn(true);

        // when
        boolean result = userController.validateUser(userId);

        // then
        assertTrue(result);
        verify(userService).validateUser(userId);
    }
}
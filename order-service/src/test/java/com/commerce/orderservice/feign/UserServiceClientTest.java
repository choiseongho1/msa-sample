package com.commerce.orderservice.feign;

import com.commerce.common.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

class UserServiceClientTest {

    @Mock
    private UserServiceClient userServiceClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("사용자 검증 테스트 - 성공")
    void validateUserTest_Success() {
        // given
        String userId = "user123";

        given(userServiceClient.validateUser(userId)).willReturn(true);

        // when
        boolean result = userServiceClient.validateUser(userId);

        // then
        assertTrue(result);
        verify(userServiceClient, times(1)).validateUser(userId);
    }

    @Test
    @DisplayName("사용자 검증 테스트 - 실패")
    void validateUserTest_Failure() {
        // given
        String userId = "user123";

        given(userServiceClient.validateUser(userId)).willReturn(false);

        // when
        boolean result = userServiceClient.validateUser(userId);

        // then
        assertFalse(result);
        verify(userServiceClient, times(1)).validateUser(userId);
    }

    @Test
    @DisplayName("사용자 검증 테스트 - 예외 발생")
    void validateUserTest_Exception() {
        // given
        String userId = "user123";

        given(userServiceClient.validateUser(userId)).willThrow(new CustomException("사용자 검증 실패"));

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            userServiceClient.validateUser(userId);
        });

        assertEquals("사용자 검증 실패", exception.getMessage());
        verify(userServiceClient, times(1)).validateUser(userId);
    }
}
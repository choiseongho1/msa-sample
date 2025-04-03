package com.commerce.userservice.security;

import com.commerce.common.dto.TokenDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenUtilTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private JwtTokenUtil jwtTokenUtil;

    private final String secretKey = "testSecretKeyWithAtLeast32Characters1234567890";
    private final String userId = "testuser";

    @BeforeEach
    void setUp() {
        // secretKey 설정
        ReflectionTestUtils.setField(jwtTokenUtil, "secretKey", secretKey);

        // RedisTemplate 설정
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("토큰 생성 테스트")
    void createTokenTest() {
        // given
        doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), any());

        // when
        TokenDto tokenDto = jwtTokenUtil.createToken(userId);

        // then
        assertNotNull(tokenDto);
        assertNotNull(tokenDto.getAccessToken());
        assertNotNull(tokenDto.getRefreshToken());
        verify(redisTemplate).opsForValue();
        verify(valueOperations).set(eq("RT:" + userId), anyString(), anyLong(), any());
    }

    @Test
    @DisplayName("토큰에서 사용자 ID 추출 테스트")
    void getUserIdFromTokenTest() {
        // given
        JwtTokenUtil spyJwtTokenUtil = spy(jwtTokenUtil);
        doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), any());
        TokenDto tokenDto = spyJwtTokenUtil.createToken(userId);
        String accessToken = tokenDto.getAccessToken();

        // when
        String extractedUserId = spyJwtTokenUtil.getUserId(accessToken);

        // then
        assertEquals(userId, extractedUserId);
    }

    @Test
    @DisplayName("토큰 만료 여부 테스트 - 만료되지 않은 토큰")
    void isExpiredNotExpiredTokenTest() {
        // given
        JwtTokenUtil spyJwtTokenUtil = spy(jwtTokenUtil);
        doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), any());
        TokenDto tokenDto = spyJwtTokenUtil.createToken(userId);
        String accessToken = tokenDto.getAccessToken();

        // when
        boolean isExpired = spyJwtTokenUtil.isExpired(accessToken);

        // then
        assertFalse(isExpired);
    }

}
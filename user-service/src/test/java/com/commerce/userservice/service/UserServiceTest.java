package com.commerce.userservice.service;

import com.commerce.common.dto.TokenDto;
import com.commerce.common.exception.CustomException;
import com.commerce.userservice.dto.UserDetailDto;
import com.commerce.userservice.dto.UserLoginDto;
import com.commerce.userservice.dto.UserSaveDto;
import com.commerce.userservice.entity.User;
import com.commerce.userservice.repository.UserRepository;
import com.commerce.userservice.security.JwtTokenUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("회원가입 성공 테스트")
    void createUserSuccess() {
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

        given(userRepository.findByUserIdOrUserEmail(anyString(), anyString())).willReturn(Optional.empty());
        given(userRepository.save(any(User.class))).willReturn(null); // 리턴값은 중요하지 않음

        // when & then
        assertDoesNotThrow(() -> userService.createUser(userSaveDto));
        verify(userRepository).findByUserIdOrUserEmail(userSaveDto.getUserId(), userSaveDto.getUserEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 테스트 - 중복된 ID 또는 이메일")
    void createUserFailDuplicate() {
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

        User existingUser = User.builder()
            .userId("testuser")
            .userEmail("test@example.com")
            .userPassword("password123!@#")
            .userName("Existing User")
            .userAddress("Existing Address")
            .userZipcode("54321")
            .userPhone("01087654321")
            .userRole(User.Role.USER)
            .userCreatedAt(LocalDateTime.now())
            .userUpdatedAt(LocalDateTime.now())
            .build();

        given(userRepository.findByUserIdOrUserEmail(anyString(), anyString())).willReturn(Optional.of(existingUser));

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> userService.createUser(userSaveDto));
        assertEquals("이미 존재하는 ID 혹은 EMAIL입니다.", exception.getMessage());
        verify(userRepository).findByUserIdOrUserEmail(userSaveDto.getUserId(), userSaveDto.getUserEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    void loginSuccess() {
        // given
        UserLoginDto userLoginDto = UserLoginDto.builder()
            .userId("testuser")
            .userPassword("password123!@#")
            .build();

        User user = User.builder()
            .userId("testuser")
            .userEmail("test@example.com")
            .userPassword("password123!@#")
            .userName("Test User")
            .userAddress("Test Address")
            .userZipcode("12345")
            .userPhone("01012345678")
            .userRole(User.Role.USER)
            .userCreatedAt(LocalDateTime.now())
            .userUpdatedAt(LocalDateTime.now())
            .build();

        TokenDto tokenDto = TokenDto.builder()
            .accessToken("test-access-token")
            .refreshToken("test-refresh-token")
            .build();

        given(userRepository.findByUserIdOrUserPassword(anyString(), anyString())).willReturn(Optional.of(user));
        given(jwtTokenUtil.createToken(anyString())).willReturn(tokenDto);

        // when
        TokenDto result = userService.login(userLoginDto);

        // then
        assertNotNull(result);
        assertEquals(tokenDto.getAccessToken(), result.getAccessToken());
        assertEquals(tokenDto.getRefreshToken(), result.getRefreshToken());
        verify(userRepository).findByUserIdOrUserPassword(userLoginDto.getUserId(), userLoginDto.getUserPassword());
        verify(jwtTokenUtil).createToken(userLoginDto.getUserId());
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 사용자 정보 없음")
    void loginFailUserNotFound() {
        // given
        UserLoginDto userLoginDto = UserLoginDto.builder()
            .userId("testuser")
            .userPassword("password123!@#")
            .build();

        given(userRepository.findByUserIdOrUserPassword(anyString(), anyString())).willReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> userService.login(userLoginDto));
        assertEquals("올바르지 않은 사용자 정보입니다.", exception.getMessage());
        verify(userRepository).findByUserIdOrUserPassword(userLoginDto.getUserId(), userLoginDto.getUserPassword());
        verify(jwtTokenUtil, never()).createToken(anyString());
    }

    @Test
    @DisplayName("사용자 정보 조회 성공 테스트")
    void getLoginUserByUserIdSuccess() {
        // given
        String userId = "testuser";
        User user = User.builder()
            .userId(userId)
            .userEmail("test@example.com")
            .userPassword("password123!@#")
            .userName("Test User")
            .userAddress("Test Address")
            .userZipcode("12345")
            .userPhone("01012345678")
            .userRole(User.Role.USER)
            .userCreatedAt(LocalDateTime.now())
            .userUpdatedAt(LocalDateTime.now())
            .build();

        given(userRepository.findByUserId(anyString())).willReturn(Optional.of(user));

        // when
        UserDetailDto result = userService.getLoginUserByUserId(userId);

        // then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(user.getUserEmail(), result.getUserEmail());
        assertEquals(user.getUserName(), result.getUserName());
        assertEquals(user.getUserAddress(), result.getUserAddress());
        assertEquals(user.getUserZipcode(), result.getUserZipcode());
        assertEquals(user.getUserPhone(), result.getUserPhone());
        verify(userRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("사용자 정보 조회 실패 테스트 - 사용자 정보 없음")
    void getLoginUserByUserIdFailUserNotFound() {
        // given
        String userId = "testuser";
        given(userRepository.findByUserId(anyString())).willReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> userService.getLoginUserByUserId(userId));
        assertEquals("올바르지 않은 사용자 정보입니다.", exception.getMessage());
        verify(userRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("사용자 유효성 검증 테스트 - 존재하는 사용자")
    void validateUserExists() {
        // given
        String userId = "testuser";
        given(userRepository.existsById(anyString())).willReturn(true);

        // when
        boolean result = userService.validateUser(userId);

        // then
        assertTrue(result);
        verify(userRepository).existsById(userId);
    }

    @Test
    @DisplayName("사용자 유효성 검증 테스트 - 존재하지 않는 사용자")
    void validateUserNotExists() {
        // given
        String userId = "nonexistentuser";
        given(userRepository.existsById(anyString())).willReturn(false);

        // when
        boolean result = userService.validateUser(userId);

        // then
        assertFalse(result);
        verify(userRepository).existsById(userId);
    }
}
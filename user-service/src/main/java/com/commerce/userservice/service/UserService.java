package com.commerce.userservice.service;

import com.commerce.common.exception.CustomException;
import com.commerce.userservice.dto.UserDetailDto;
import com.commerce.userservice.dto.UserLoginDto;
import com.commerce.userservice.dto.UserSaveDto;
import com.commerce.userservice.entity.User;
import com.commerce.userservice.repository.UserRepository;
import com.commerce.userservice.security.JwtTokenUtil;
import com.commerce.common.dto.TokenDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * User 관련 Service
 *
 * @author : choiseongho
 * @fileName : UserService.java
 * @since : 2024-12-31
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    // User 관련 Repository
    private final UserRepository userRepository;
    private final JwtTokenUtil jwtTokenUtil;

    /**
     * User 정보 저장
     *
     * @param userSaveDto 입력된 User 정보
     */
    @Transactional
    public void createUser(UserSaveDto userSaveDto) {
        // 1. ID & EMAIL 중복 검사
        Optional<User> user = userRepository.findByUserIdOrUserEmail(userSaveDto.getUserId(), userSaveDto.getUserEmail());

        if(user.isPresent()) {
            throw new CustomException("이미 존재하는 ID 혹은 EMAIL입니다.");
        }

        // 2. User Table에 저장
        userRepository.save(userSaveDto.toEntity());
    }


    /**
     * 회원 로그인 처리
     *
     * @param userLoginDto 로그인 시도 정보
     */
    @Transactional
    public TokenDto login(UserLoginDto userLoginDto) {
        Optional<User> user = userRepository.findByUserIdOrUserPassword(userLoginDto.getUserId(), userLoginDto.getUserPassword());

        if(user.isEmpty()){
            throw new CustomException("올바르지 않은 사용자 정보입니다.");
        }

        // 로그인 성공 => Jwt Token 발급
        return jwtTokenUtil.createToken(userLoginDto.getUserId());
    }

    /**
     * 회원 정보 조회
     *
     * @param userId the user id
     */
    @Transactional(readOnly = true)
    public UserDetailDto getLoginUserByUserId(String userId) {
        User findUser = userRepository.findByUserId(userId).orElseThrow(() -> new CustomException("올바르지 않은 사용자 정보입니다."));
        return UserDetailDto.fromEntity(findUser);

    }

    // Feign Client 관련 영역
    @Transactional(readOnly = true)
    public boolean validateUser(String userId) {
        return userRepository.existsById(userId);
    }
}

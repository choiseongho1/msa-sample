package com.commerce.userservice.controller;

import com.commerce.common.dto.ResponseDto;
import com.commerce.userservice.dto.UserSaveDto;
import com.commerce.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * User-service의 Controller
 *
 * @author : choiseongho
 * @fileName : UserController.java
 * @since : 2024-12-31
 */

@Slf4j
@Tag(name = "User", description = "사용자 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    // User 관련 Service
    private final UserService userService;


    /**
     * 회원가입 API
     *
     * @param userSaveDto
     */
    @PostMapping("/signup")
    @Operation(summary = "회원가입")
    public ResponseDto<?> signup(@Valid @RequestBody UserSaveDto userSaveDto) {
        userService.createUser(userSaveDto);
        return ResponseDto.ok();
    }


    /**
     * 로그인 사용자 정보 조회 API
     *
     * @param userId
     */
    @GetMapping("/{userId}")
    @Operation(summary = "로그인 사용자 정보 조회")
    public ResponseDto<?> getLoginUserByUserId(@PathVariable(name = "userId" ) String userId) {
        return ResponseDto.ok(userService.getLoginUserByUserId(userId));
    }


    // ------------------------------------
    // Feign Client 영역
    // ------------------------------------


    /**
     * 유효 사용자 검증
     *
     * @param id
     */
    @GetMapping("/validate/{id}")
    @Operation(summary = "[Feign] 유효 사용자 검증")
    public boolean validateUser(@PathVariable(name = "id" ) String id) {
        return userService.validateUser(id);
    }
}

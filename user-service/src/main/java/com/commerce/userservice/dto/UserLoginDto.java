package com.commerce.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

/**
 * 사용자 Login 요청 DTO
 *
 * @author : choiseongho
 * @fileName : UserLoginDto.java
 * @since : 2024-12-31
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginDto {

    // 사용자 ID
    @Schema(description = "아이디", example = "testID")
    @NotBlank(message = "아이디는 필수입니다.")
    private String userId;

    // 비밀번호
    @Schema(description = "비밀번호", example = "password123!@#")
    @NotBlank(message = "비밀번호는 필수입니다.")
    @NotEmpty
    private String userPassword;
}

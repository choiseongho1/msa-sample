package com.commerce.userservice.dto;

import com.commerce.userservice.entity.User;
import com.commerce.userservice.valid.email.Email;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 사용자 Save Dto
 *
 * @author : choiseongho
 * @fileName : UserSaveDto.java
 * @since : 2024-12-31
 */
@Builder
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class UserSaveDto {

    // 사용자 ID
    @Schema(description = "아이디", example = "testID")
    @NotBlank(message = "아이디는 필수입니다.")
    private String userId;

    // 비밀번호
    @Schema(description = "비밀번호", example = "password123!@#")
    @NotBlank(message = "비밀번호는 필수입니다.")
    private String userPassword;

    // 이메일
    @Schema(description = "이메일", example = "testID@example.com")
    @NotBlank(message = "이메일은 필수입니다.")
    private String userEmail;

    // 회원 이름
    @Schema(description = "회원 이름", example = "testName")
    @NotBlank(message = "회원 이름은 필수입니다.")
    private String userName;

    // 회원 주소
    @Schema(description = "회원 주소", example = "testAddress")
    @NotBlank(message = "회원 주소는 필수입니다.")
    private String userAddress;

    // 우편번호
    @Schema(description = "우편번호", example = "12345")
    @NotBlank(message = "우편번호는 필수입니다.")
    private String userZipcode;

    // 전화번호
    @Schema(description = "전화번호", example = "01012345678")
    @NotBlank(message = "전화번호는 필수입니다.")
    private String userPhone;


    public User toEntity() {
        return User.builder()
                .userId(userId)
                .userPassword(userPassword)
                .userEmail(userEmail)
                .userName(userName)
                .userAddress(userAddress)
                .userZipcode(userZipcode)
                .userPhone(userPhone)
                .userRole(User.Role.valueOf("USER"))
                .userCreatedAt(LocalDateTime.now())
                .userUpdatedAt(LocalDateTime.now())
                .build();
    }

}

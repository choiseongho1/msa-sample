package com.commerce.userservice.dto;

import com.commerce.userservice.entity.User;
import com.commerce.userservice.valid.email.Email;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 사용자 Detail Dto
 *
 * @author : choiseongho
 * @fileName : UserDetailDto.java
 * @since : 2024-12-31
 */
@Builder
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class UserDetailDto {

    // 사용자 ID
    @Schema(description = "아이디", example = "testID")
    private String userId;

    // 이메일
    @Schema(description = "이메일", example = "testID@example.com")
    private String userEmail;

    // 회원 이름
    @Schema(description = "회원 이름", example = "testName")
    private String userName;

    // 회원 주소
    @Schema(description = "회원 주소", example = "testAddress")
    private String userAddress;

    // 우편번호
    @Schema(description = "우편번호", example = "12345")
    private String userZipcode;

    // 전화번호
    @Schema(description = "전화번호", example = "01012345678")
    private String userPhone;


    public static UserDetailDto fromEntity(User user) {
        return UserDetailDto.builder()
                .userId(user.getUserId())
                .userEmail(user.getUserEmail())
                .userName(user.getUserName())
                .userAddress(user.getUserAddress())
                .userZipcode(user.getUserZipcode())
                .userPhone(user.getUserPhone())
                .build();
    }

}

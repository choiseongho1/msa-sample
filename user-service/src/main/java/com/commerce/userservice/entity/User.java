package com.commerce.userservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * User Table Entity
 *
 * @author : choiseongho
 * @fileName : User.java
 * @since : 2024-12-31
 */
@Entity
@Table(name = "users")
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor @Builder
public class User  {

    @Id // 기본 키 설정
    @Column(name = "user_id", nullable = false, length = 90, unique = true)
    // 사용자 ID
    private String userId;

    @Column(name = "user_password", nullable = false, length = 255)
    // 비밀번호
    private String userPassword;

    @Column(name = "user_email", nullable = false, length = 100, unique = true)
    // 이메일
    private String userEmail;

    @Column(name = "user_name", nullable = false, length = 50)
    // 회원 이름
    private String userName;

    @Column(name = "user_address", nullable = false, length = 255)
    // 회원 주소
    private String userAddress;

    @Column(name = "user_zipcode", nullable = false, length = 20)
    // 우편번호
    private String userZipcode;

    @Column(name = "user_phone", nullable = false, length = 20)
    // 전화번호
    private String userPhone;

    @Enumerated(EnumType.STRING) // Enum 타입을 문자열로 저장
    @Column(name = "user_role", nullable = false)
    // 회원 역할(Role)
    private Role userRole;

    @Column(name = "user_created_at", updatable = false)
    // 계정 생성 날짜, 생성 후 수정 불가
    private LocalDateTime userCreatedAt;

    @Column(name = "user_updated_at", nullable = false)
    // 계정 수정 날짜
    private LocalDateTime userUpdatedAt;

    // Enum 정의 (회원 역할)
    public enum Role {
        USER, // 일반 사용자
        ADMIN // 관리자
    }
}

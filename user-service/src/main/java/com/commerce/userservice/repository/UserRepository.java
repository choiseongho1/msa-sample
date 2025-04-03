package com.commerce.userservice.repository;

import com.commerce.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * User Table Repository
 *
 * @author : choiseongho
 * @fileName : UserRepository.java
 * @since : 2024-12-31
 */
public interface UserRepository extends JpaRepository<User, String> {

    // 사용자 ID 또는 EMAIL 중복 여부
    Optional<User> findByUserIdOrUserEmail(String userId, String userEmail);

    // 사용자 ID 과 PASSWORD 로 로그인 시도
    Optional<User> findByUserIdOrUserPassword(String userId, String password);

    // 사용자 ID로 로그인 시도
    Optional<User> findByUserId(String userId);

}

package com.commerce.userservice.repository;

import com.commerce.userservice.entity.User;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * User Table Repository Test
 * @author : choiseongho
 * @fileName : UserRepositoryTest.java
 * @since : 2024-12-31
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;


    public User makeTestUser() {
        // 테스트 데이터 1
        return User.builder()
                .userId("user001")
                .userPassword("password001")
                .userEmail("user001@example.com")
                .userName("John Doe")
                .userAddress("123 Main St")
                .userZipcode("12345")
                .userPhone("010-1111-1111")
                .userRole(User.Role.USER)
                .userCreatedAt(LocalDateTime.now().minusDays(10))
                .userUpdatedAt(LocalDateTime.now())
                .build();
    }

    @DisplayName("User 테이블에 정상적으로 저장")
    @Test
    public void save(){
        // given
        User user = makeTestUser();
        // when
        User savedUser = userRepository.save(user);
        // then
        assertEquals(user.getUserId(), savedUser.getUserId());
    }

    @DisplayName("User 테이블에 정상적으로 저장되지 않음 (unique 중복)")
    @Test
    public void save_fail_unique(){
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            // given
            User user1 = makeTestUser();
            userRepository.save(user1);

            // when
            User user2 = makeTestUser();
            userRepository.save(user2);
        });
    }

    @DisplayName("User 테이블에 정상적으로 저장되지 않음 (nullable)")
    @Test
    public void save_fail_nullable(){
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            // given
            User user1 = makeTestUser();

            user1.setUserAddress(null);
            userRepository.save(user1);

        });
    }



}
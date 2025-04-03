package com.commerce.userservice.valid.email;

import jakarta.validation.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Email 유효성 체크를 하기 위한 custom annotation
 *
 * @author : choiseongho
 * @fileName : Email.java
 * @since : 2024-12-31
 */
@Target({ElementType.PARAMETER, ElementType.FIELD}) // 어노테이션이 적용될 수 있는 대상
@Retention(RetentionPolicy.RUNTIME)                 // 어노테이션 정보가 유지되는 기간
@Constraint(validatedBy = EmailValidator.class)     // 해당 어노테이션을 사용하여 유효성 검사를 수행할 클래스를 지정
public @interface Email {
    String message() default "";

    Class[] groups() default {};

    Class[] payload() default {};
}
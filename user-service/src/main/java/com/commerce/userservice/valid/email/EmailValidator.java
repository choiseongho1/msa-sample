package com.commerce.userservice.valid.email;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * explain the Class File
 *
 * @author : choiseongho
 * @fileName : EmailValidator.java
 * @since : 2024-12-31
 */
public class EmailValidator implements ConstraintValidator<Email, String> { //Email.java 참조, String 타입의 값의 유효성을 검사
    public static final String EMAIL_REGEX = "[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-.]?[0-9a-zA-Z])*\\.[a-zA-Z]{2,3}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if(StringUtils.isEmpty(value))
            return false;

        List<String> emails = Arrays.asList(value.split(";"));
        if(emails.isEmpty()) return false;

        return emails.stream()
                .allMatch((email) -> Pattern.matches(EMAIL_REGEX, email));

    }
}
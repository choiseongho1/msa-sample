package com.commerce.productservice.util;

import java.util.List;

import org.springframework.util.StringUtils;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;

/**
 * Query DSL 관련 Util
 */
public class QuerydslUtil {
    /**
     * in 표현식 : in (A, B, C)
     */
    public static <T> BooleanExpression in(SimpleExpression<T> path, List<T> values) {
        return values != null && !values.isEmpty() ? path.in(values) : null;
    }

    /**
     * in 표현식 : in (A, B, C)
     */
    public static <T extends Enum<T>> BooleanExpression in(EnumPath<T> path, List<T> values) {
        return values != null && !values.isEmpty() ? path.in(values) : null;
    }

    /**
     * like 표현식 : '%DATA%'
     */
    public static BooleanExpression contains(StringPath path, String val) {
        return StringUtils.hasText(val) ? path.contains(val) : null;
    }

    /**
     * like 표현식 : '%DATA%'
     */
    public static BooleanExpression contains(StringExpression path, String val) {
        return StringUtils.hasText(val) ? path.contains(val) : null;
    }

    /**
     * between 표현식 : between A and B
     */
    public static BooleanExpression between(NumberPath<Integer> path, Integer fromVal, Integer toVal) {
        if (fromVal != null && toVal != null) {
            return path.between(fromVal, toVal);
        } else if (fromVal != null) {
            return path.goe(fromVal);
        } else if (toVal != null) {
            return path.loe(toVal);
        }
        return null;
    }

    /**
     * greater than or equal 표현식 : >=
     */
    public static BooleanExpression goe(NumberPath<Integer> path, Integer value) {
        return value != null ? path.goe(value) : null;
    }

    /**
     * less than or equal 표현식 : <=
     */
    public static BooleanExpression loe(NumberPath<Integer> path, Integer value) {
        return value != null ? path.loe(value) : null;
    }
}
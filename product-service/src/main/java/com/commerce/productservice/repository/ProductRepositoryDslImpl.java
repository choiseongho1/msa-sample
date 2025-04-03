package com.commerce.productservice.repository;

import static com.commerce.productservice.entity.QProduct.product;
import static com.commerce.productservice.entity.QWishlist.wishlist;

import com.commerce.productservice.entity.QWishlist;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import com.commerce.productservice.dto.ProductListDto;
import com.commerce.productservice.dto.ProductSearchDto;
import com.commerce.productservice.dto.QProductListDto;
import com.commerce.productservice.entity.Product.ProductCategory;
import com.commerce.productservice.entity.Product.ProductStatus;
import com.commerce.productservice.util.QuerydslUtil;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ProductRepositoryDslImpl implements ProductRepositoryDsl {
     private final JPAQueryFactory queryFactory;



    @Override
    public Page<ProductListDto> findProductsList(ProductSearchDto searchDto, Pageable pageable) {
        final Long totalCount = getCountQuery(searchDto)
                .fetchOne();

        final List<ProductListDto> prodcutList = getListQuery(searchDto)
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        return new PageImpl<ProductListDto>(prodcutList, pageable, (totalCount==null)?0:totalCount);

    }


    private JPAQuery<Long> getCountQuery(ProductSearchDto searchDto) {
        return queryFactory.select(product.count())
                .from(product)
                .where(getWherePredicate(searchDto));
    }


    private JPAQuery<ProductListDto> getListQuery(ProductSearchDto searchDto) {
        QWishlist wishlistSub = new QWishlist("wishlistSub");

        return queryFactory
                .select(new QProductListDto(
                        product.id,
                        product.title,
                        product.imageId,
                        product.content,
                        product.status,
                        product.wishlistCount,
                        product.price,
                        product.category
                ))
                .from(product)
                .where(getWherePredicate(searchDto))
                .orderBy(product.id.desc());
    }

    /**
     * 검색 조건에 따른 where절 생성
     */
    private BooleanExpression[] getWherePredicate(ProductSearchDto searchDto) {
        return new BooleanExpression[]{
                titleContains(searchDto.getTitle()),
                categoryIn(searchDto.getCategory()),
                statusIn(searchDto.getStatus()),
                priceBetween(searchDto.getMinPrice(), searchDto.getMaxPrice()),
        };
    }

    private BooleanExpression titleContains(String title) {
        return QuerydslUtil.contains(product.title, title);

    }

    private BooleanExpression categoryIn(List<ProductCategory> category) {
        return QuerydslUtil.in(product.category, category);
    }

    private BooleanExpression statusIn(List<ProductStatus> status) {
        return QuerydslUtil.in(product.status, status);
    }

    private BooleanExpression priceBetween(Integer minPrice, Integer maxPrice) {
        return QuerydslUtil.between(product.price, minPrice, maxPrice);
    }
}
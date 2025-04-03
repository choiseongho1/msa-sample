package com.commerce.productservice.repository;

import static com.commerce.productservice.entity.QWishlist.wishlist;
import static com.commerce.productservice.entity.QProduct.product;
import static com.commerce.productservice.entity.QProductOption.productOption;

import com.commerce.productservice.dto.QWishlistListDto;
import com.commerce.productservice.dto.WishlistListDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class WishlistRepositoryDslImpl implements WishlistRepositoryDsl {
     private final JPAQueryFactory queryFactory;

     @Override
     public List<WishlistListDto> findWishlistList(String userId){
          return queryFactory
              .select(new QWishlistListDto(
                    wishlist.wishlistId,
                    wishlist.productId,
                    product.title,
                    productOption.price
              ))
              .from(wishlist)
              .join(product).on(product.id.eq(wishlist.productId))
              .join(productOption).on(productOption.product.eq(product))
              .where(wishlist.userId.eq(userId))
              .orderBy(wishlist.wishlistId.desc())
              .fetch();
//          return null;
     }

}
package com.commerce.productservice.service;

import com.commerce.common.exception.CustomException;
import com.commerce.productservice.dto.WishlistListDto;
import com.commerce.productservice.dto.WishlistSaveDto;
import com.commerce.productservice.entity.Wishlist;
import com.commerce.productservice.repository.WishlistRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WishlistService {


    private final WishlistRepository wishlistRepository;


    /**
     * 위시리스트 등록 service
     *
     * @param wishlistSaveDto
     */
    @CacheEvict(cacheNames = "wishlist", key = "#p0.userId")
    public void saveWishlist(WishlistSaveDto wishlistSaveDto) {
        wishlistRepository.findByUserIdAndProductId(wishlistSaveDto.getUserId(), wishlistSaveDto.getProductId()).ifPresent(wishlist -> {
            throw new CustomException("이미 위시리스트에 추가된 상품입니다.");
        });

        Wishlist wishlist = wishlistSaveDto.toEntity();
        wishlistRepository.save(wishlist);

    }

    /**
     * 위시리스트 삭제 service
     *
     * @param wishlistId
     */

    @CacheEvict(cacheNames = "wishlist", key = "#p0")
    public void removeFromWishlist(String userId, Long wishlistId) {
        wishlistRepository.deleteById(wishlistId);
    }


    /**
     * 위시리스트 조회 service
     *
     * @param userId the user id
     */
    @Cacheable(cacheNames = "wishlist",  key = "#p0", condition = "#p0 != null")
    public List<WishlistListDto> findWishlist(String userId) {
        return wishlistRepository.findWishlistList(userId);
    }


}

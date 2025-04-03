package com.commerce.productservice.service;

import com.commerce.common.exception.CustomException;
import com.commerce.productservice.dto.WishlistListDto;
import com.commerce.productservice.dto.WishlistSaveDto;
import com.commerce.productservice.entity.Wishlist;
import com.commerce.productservice.repository.WishlistRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @InjectMocks
    private WishlistService wishlistService;

    @Test
    @DisplayName("위시리스트 등록 성공 테스트")
    void saveWishlistTest() {
        // given
        WishlistSaveDto wishlistSaveDto = new WishlistSaveDto();
        wishlistSaveDto.setUserId("user123");
        wishlistSaveDto.setProductId(1L);

        Wishlist wishlist = wishlistSaveDto.toEntity();

        given(wishlistRepository.findByUserIdAndProductId(anyString(), anyLong())).willReturn(Optional.empty());
        given(wishlistRepository.save(any(Wishlist.class))).willReturn(wishlist);

        // when
        wishlistService.saveWishlist(wishlistSaveDto);

        // then
        verify(wishlistRepository).findByUserIdAndProductId(wishlistSaveDto.getUserId(), wishlistSaveDto.getProductId());
        verify(wishlistRepository).save(any(Wishlist.class));
    }

    @Test
    @DisplayName("위시리스트 등록 실패 테스트 - 이미 존재하는 위시리스트")
    void saveWishlistTest_AlreadyExists() {
        // given
        WishlistSaveDto wishlistSaveDto = new WishlistSaveDto();
        wishlistSaveDto.setUserId("user123");
        wishlistSaveDto.setProductId(1L);

        Wishlist existingWishlist = wishlistSaveDto.toEntity();

        given(wishlistRepository.findByUserIdAndProductId(anyString(), anyLong())).willReturn(Optional.of(existingWishlist));

        // when & then
        assertThrows(CustomException.class, () -> wishlistService.saveWishlist(wishlistSaveDto));
        verify(wishlistRepository).findByUserIdAndProductId(wishlistSaveDto.getUserId(), wishlistSaveDto.getProductId());
    }

    @Test
    @DisplayName("위시리스트 삭제 성공 테스트")
    void removeFromWishlistTest() {
        // given
        String userId = "user123";
        Long wishlistId = 1L;

        doNothing().when(wishlistRepository).deleteById(anyLong());

        // when
        wishlistService.removeFromWishlist(userId, wishlistId);

        // then
        verify(wishlistRepository).deleteById(wishlistId);
    }

    @Test
    @DisplayName("위시리스트 조회 성공 테스트")
    void findWishlistTest() {
        // given
        String userId = "user123";

        List<WishlistListDto> wishlistList = new ArrayList<>();
        WishlistListDto wishlist = new WishlistListDto();
        wishlist.setWishlistId(1L);
        wishlist.setProductId(1L);
        wishlist.setProductTitle("테스트 상품");
        wishlist.setPrice(10000);
        wishlistList.add(wishlist);

        given(wishlistRepository.findWishlistList(anyString())).willReturn(wishlistList);

        // when
        List<WishlistListDto> result = wishlistService.findWishlist(userId);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("테스트 상품", result.get(0).getProductTitle());
        verify(wishlistRepository).findWishlistList(userId);
    }
}
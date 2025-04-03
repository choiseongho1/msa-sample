package com.commerce.productservice.controller;

import com.commerce.common.dto.ResponseDto;
import com.commerce.productservice.dto.WishlistListDto;
import com.commerce.productservice.dto.WishlistSaveDto;
import com.commerce.productservice.service.WishlistService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WishlistControllerTest {

    @Mock
    private WishlistService wishlistService;

    @InjectMocks
    private WishlistController wishlistController;

    @Test
    @DisplayName("위시리스트 추가 API 테스트")
    void saveWishlistTest() {
        // given
        WishlistSaveDto wishlistSaveDto = new WishlistSaveDto();
        wishlistSaveDto.setUserId("user123");
        wishlistSaveDto.setProductId(1L);

        doNothing().when(wishlistService).saveWishlist(any(WishlistSaveDto.class));

        // when
        ResponseDto<?> response = wishlistController.saveWishlist(wishlistSaveDto);

        // then
        assertEquals(200, response.getCode());
        verify(wishlistService).saveWishlist(wishlistSaveDto);
    }

    @Test
    @DisplayName("위시리스트 조회 API 테스트")
    void findWishlistTest() {
        // given
        String userId = "user123";

        List<WishlistListDto> wishlistList = new ArrayList<>();
        WishlistListDto wishlist = WishlistListDto.builder()
            .wishlistId(1L)
            .productId(1L)
            .productTitle("테스트 상품")
            .price(10000)
            .build();

        wishlistList.add(wishlist);

        given(wishlistService.findWishlist(anyString())).willReturn(wishlistList);

        // when
        ResponseDto<?> response = wishlistController.findWishlist(userId);

        // then
        assertEquals(200, response.getCode());
        verify(wishlistService).findWishlist(userId);
    }

    @Test
    @DisplayName("위시리스트 삭제 API 테스트")
    void removeFromWishlistTest() {
        // given
        String userId = "user123";
        Long wishlistId = 1L;

        doNothing().when(wishlistService).removeFromWishlist(anyString(), anyLong());

        // when
        ResponseDto<?> response = wishlistController.removeFromWishlist(userId, wishlistId);

        // then
        assertEquals(200, response.getCode());
        verify(wishlistService).removeFromWishlist(userId, wishlistId);
    }
}
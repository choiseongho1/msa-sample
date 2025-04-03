package com.commerce.productservice.controller;

import com.commerce.common.dto.ResponseDto;
import com.commerce.productservice.dto.WishlistSaveDto;
import com.commerce.productservice.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishlistController {
    private final WishlistService wishlistService;

    /**
     * 위시리스트 추가 API
     *
     * @param wishlistSaveDto the wishlist save dto
     */
    @PostMapping
    @Operation(summary = "위시리스트 추가")
    public ResponseDto<?> saveWishlist(@RequestBody WishlistSaveDto wishlistSaveDto) {
        wishlistService.saveWishlist(wishlistSaveDto);
        return ResponseDto.ok();
    }

    /**
     * 위시리스트 조회 API
     *
     * @param userId the user id
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "위시리스트 조회")
    public ResponseDto<?> findWishlist(@PathVariable(name = "userId") String userId) {
        return ResponseDto.ok(wishlistService.findWishlist(userId));
    }

    /**
     * 위시리스트 삭제 API
     *
     * @param userId     the user id
     * @param wishlistId the wishlist id
     */
    @DeleteMapping("/user/{userId}/{wishlistId}")
    @Operation(summary = "위시리스트 삭제")
    public ResponseDto<?> removeFromWishlist(@PathVariable(name = "userId") String userId, @PathVariable(name = "wishlistId") Long wishlistId) {
        wishlistService.removeFromWishlist(userId, wishlistId);
        return ResponseDto.ok();
    }
}

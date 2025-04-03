package com.commerce.cartservice.controller;

import com.commerce.cartservice.dto.CartItemListDto;
import com.commerce.cartservice.dto.CartItemSaveDto;
import com.commerce.cartservice.dto.CartItemUpdateQuantityDto;
import com.commerce.cartservice.service.CartService;
import com.commerce.common.dto.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/cart")
@RestController
public class CartController {

    private final CartService cartService;

    @GetMapping("/{userId}")
    @Operation(summary = "[로그인] 사용자 장바구니 목록 조회")
    public ResponseDto<?> findCartItemList(
        @PathVariable String userId) {
        List<CartItemListDto> cartItemList = cartService.findCartItemList(userId);
        return ResponseDto.ok(cartItemList);
    }

    /**
     * 장바구니 상품 API.
     *
     * @param userId
     * @param cartItemSaveDtoList
     */
    @PostMapping("/{userId}")
    @Operation(summary = "[로그인] 사용자 장바구니 저장")
    public ResponseDto<?> saveCartItem(
        @PathVariable String userId,
        @RequestBody List<CartItemSaveDto> cartItemSaveDtoList) {

        cartService.saveCartItem(userId, cartItemSaveDtoList);
        return ResponseDto.ok();
    }

    /**
     * 장바구니 수량 수정 API
     *
     * @param cartItemUpdateQuantityDto
     */
    @PutMapping("/item")
    @Operation(summary = "[로그인] 사용자 장바구니 상품 수량 변경")
    public ResponseDto<?> updateCartItemQuantity(
        @RequestBody CartItemUpdateQuantityDto cartItemUpdateQuantityDto) {
        cartService.updateCartItemQuantity(cartItemUpdateQuantityDto);
        return ResponseDto.ok();
    }

    /**
     * 장바구니 상품 삭제 API
     *
     * @param cartItemId
     */
    @DeleteMapping("/{userId}/item/{cartItemId}")
    @Operation(summary = "[로그인] 사용자 장바구니 상품 삭제")
    public ResponseDto<?> removeCartItem(@PathVariable(name = "userId" ) String userId, @PathVariable(name = "cartItemId" ) Long cartItemId) {
        cartService.removeCartItem(userId, cartItemId);
        return ResponseDto.ok();
    }




    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome to the Cart service.";
    }

}

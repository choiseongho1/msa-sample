package com.commerce.cartservice.controller;

import com.commerce.cartservice.dto.GuestCartItemDeleteDto;
import com.commerce.cartservice.dto.GuestCartItemSaveDto;
import com.commerce.cartservice.entity.CartItem;
import com.commerce.cartservice.service.GuestCartService;
import com.commerce.common.dto.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/guest/cart")
public class GuestCartController {

    private final GuestCartService guestCartService;

    @GetMapping("/{sessionId}")
    @Operation(summary = "[비로그인] 사용자 장바구니 목록 조회")
    public ResponseDto<?> findGuestCart(@PathVariable String sessionId) {
        return ResponseDto.ok(guestCartService.findGuestCart(sessionId));
    }

    @PostMapping
    @Operation(summary = "[비로그인] 사용자 장바구니 상품 등록")
    public ResponseDto<?> saveGuestCart(@RequestBody GuestCartItemSaveDto guestCartItemSaveDto) {
        guestCartService.saveGuestCart(guestCartItemSaveDto);
        return ResponseDto.ok();
    }

    @DeleteMapping
    @Operation(summary = "[비로그인] 사용자 장바구니 상품 삭제")
    public ResponseDto<?> deleteGuestCart(@RequestBody GuestCartItemDeleteDto guestCartItemDeleteDto) {
        guestCartService.deleteGuestCart(guestCartItemDeleteDto);
        return ResponseDto.ok();
    }

    @DeleteMapping("/{sessionId}")
    @Operation(summary = "[비로그인] 사용자 장바구니 상품 일괄 삭제")
    public ResponseDto<?> deleteAllGuestCart(@PathVariable String sessionId) {
        guestCartService.deleteAllGuestCart(sessionId);
        return ResponseDto.ok();
    }

}

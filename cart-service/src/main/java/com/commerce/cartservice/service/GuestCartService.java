package com.commerce.cartservice.service;

import com.commerce.cartservice.dto.GuestCartItemDeleteDto;
import com.commerce.cartservice.dto.GuestCartItemListDto;
import com.commerce.cartservice.dto.GuestCartItemSaveDto;
import com.commerce.cartservice.entity.CartItem;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GuestCartService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String CART_PREFIX = "guest_cart:";

    public List<GuestCartItemListDto> findGuestCart(String sessionId) {
        String key = CART_PREFIX + sessionId;
        Object data = redisTemplate.opsForValue().get(key);
        List<CartItem> cartItems = data != null
            ? objectMapper.convertValue(data, new TypeReference<>() {})
            : List.of();

        // 엔티티 리스트를 DTO 리스트로 변환
        return cartItems.stream()
            .map(GuestCartItemListDto::of)
            .collect(Collectors.toList());
    }

    public void saveGuestCart(GuestCartItemSaveDto guestCartItemSaveDto) {
        String key = CART_PREFIX + guestCartItemSaveDto.getSessionId();
        List<CartItem> cartItems = findGuestCart(guestCartItemSaveDto.getSessionId()).stream()
            .map(dto -> CartItem.builder()
                .id(dto.getCartItemId())
                .productId(dto.getProductId())
                .productOptionId(dto.getProductOptionId())
                .quantity(dto.getQuantity())
                .build())
            .collect(Collectors.toList());

        // 기존 장바구니에 동일한 상품이 있는지 확인
        Optional<CartItem> existingItem = cartItems.stream()
            .filter(item -> item.getProductId().equals(guestCartItemSaveDto.getProductId())
                && item.getProductOptionId().equals(guestCartItemSaveDto.getProductOptionId()))
            .findFirst();

        if (existingItem.isPresent()) {
            // 기존 아이템이 있으면 수량 증가
            existingItem.get().setQuantity(existingItem.get().getQuantity() + guestCartItemSaveDto.getQuantity());
        } else {
            // 없으면 새로 추가
            cartItems.add(guestCartItemSaveDto.toEntity());
        }

        redisTemplate.opsForValue().set(key, cartItems, 1, TimeUnit.DAYS);
    }

    public void deleteGuestCart(GuestCartItemDeleteDto guestCartItemDeleteDto) {
        String key = CART_PREFIX + guestCartItemDeleteDto.getSessionId();
        List<CartItem> cartItems = findGuestCart(guestCartItemDeleteDto.getSessionId()).stream()
            .map(dto -> CartItem.builder()
                .id(dto.getCartItemId())
                .productId(dto.getProductId())
                .productOptionId(dto.getProductOptionId())
                .quantity(dto.getQuantity())
                .build())
            .collect(Collectors.toList());

        cartItems.removeIf(item -> item.getProductId().equals(guestCartItemDeleteDto.getProductId())
            && item.getProductOptionId().equals(guestCartItemDeleteDto.getProductOptionId()));
        redisTemplate.opsForValue().set(key, cartItems, 1, TimeUnit.DAYS);
    }

    public void deleteAllGuestCart(String sessionId) {
        redisTemplate.delete(CART_PREFIX + sessionId);
    }
}
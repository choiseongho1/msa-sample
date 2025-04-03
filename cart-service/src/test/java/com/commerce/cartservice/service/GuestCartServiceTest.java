package com.commerce.cartservice.service;

import com.commerce.cartservice.dto.GuestCartItemDeleteDto;
import com.commerce.cartservice.dto.GuestCartItemListDto;
import com.commerce.cartservice.dto.GuestCartItemSaveDto;
import com.commerce.cartservice.entity.CartItem;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuestCartServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private GuestCartService guestCartService;

    private final String sessionId = "test-session-id";
    private final String redisKey = "guest_cart:test-session-id";

    @Test
    @DisplayName("비회원 장바구니 조회 - 빈 장바구니")
    void findGuestCart_EmptyCart() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(redisKey)).thenReturn(null);

        // When
        List<GuestCartItemListDto> result = guestCartService.findGuestCart(sessionId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(redisTemplate).opsForValue();
        verify(valueOperations).get(redisKey);
    }

    @Test
    @DisplayName("비회원 장바구니 조회 - 상품 존재")
    void findGuestCart_WithItems() {
        // Given
        List<CartItem> cartItems = List.of(
            CartItem.builder()
                .id(1L)
                .productId(100L)
                .productOptionId(200L)
                .quantity(2)
                .build()
        );

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(redisKey)).thenReturn(cartItems);
        when(objectMapper.convertValue(any(), Mockito.<TypeReference<List<CartItem>>>any()))
            .thenReturn(cartItems);

        // When
        List<GuestCartItemListDto> result = guestCartService.findGuestCart(sessionId);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getProductId());
        assertEquals(200L, result.get(0).getProductOptionId());
        assertEquals(2, result.get(0).getQuantity());

        verify(redisTemplate).opsForValue();
        verify(valueOperations).get(redisKey);
        verify(objectMapper).convertValue(any(), Mockito.<TypeReference<List<CartItem>>>any());
    }

    @Test
    @DisplayName("비회원 장바구니에 상품 추가 - 새로운 상품")
    void saveGuestCart_NewItem() {
        // Given
        GuestCartItemSaveDto saveDto = GuestCartItemSaveDto.builder()
            .sessionId(sessionId)
            .productId(100L)
            .productOptionId(200L)
            .quantity(2)
            .build();

        List<CartItem> emptyCart = new ArrayList<>();
        List<GuestCartItemListDto> emptyCartDtos = new ArrayList<>();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(redisKey)).thenReturn(emptyCart);
        when(objectMapper.convertValue(any(), Mockito.<TypeReference<List<CartItem>>>any()))
            .thenReturn(emptyCart);

        // When
        guestCartService.saveGuestCart(saveDto);

        // Then
        verify(redisTemplate, times(2)).opsForValue();
        verify(valueOperations).get(redisKey);
        verify(valueOperations).set(eq(redisKey), any(List.class), eq(1L), eq(TimeUnit.DAYS));
    }

    @Test
    @DisplayName("비회원 장바구니에 상품 추가 - 기존 상품 수량 증가")
    void saveGuestCart_ExistingItem() {
        // Given
        GuestCartItemSaveDto saveDto = GuestCartItemSaveDto.builder()
            .sessionId(sessionId)
            .productId(100L)
            .productOptionId(200L)
            .quantity(2)
            .build();

        CartItem existingItem = CartItem.builder()
            .id(1L)
            .productId(100L)
            .productOptionId(200L)
            .quantity(3)
            .build();

        List<CartItem> cartWithExistingItem = new ArrayList<>();
        cartWithExistingItem.add(existingItem);

        GuestCartItemListDto existingItemDto = GuestCartItemListDto.of(existingItem);
        List<GuestCartItemListDto> cartDtos = new ArrayList<>();
        cartDtos.add(existingItemDto);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(redisKey)).thenReturn(cartWithExistingItem);
        when(objectMapper.convertValue(any(), Mockito.<TypeReference<List<CartItem>>>any()))
            .thenReturn(cartWithExistingItem);

        // When
        guestCartService.saveGuestCart(saveDto);

        // Then
        verify(redisTemplate, times(2)).opsForValue();
        verify(valueOperations).get(redisKey);
        verify(valueOperations).set(eq(redisKey), any(List.class), eq(1L), eq(TimeUnit.DAYS));
    }

    @Test
    @DisplayName("비회원 장바구니에서 상품 삭제")
    void deleteGuestCart() {
        // Given
        GuestCartItemDeleteDto deleteDto = GuestCartItemDeleteDto.builder()
            .sessionId(sessionId)
            .productId(100L)
            .productOptionId(200L)
            .build();

        CartItem itemToDelete = CartItem.builder()
            .id(1L)
            .productId(100L)
            .productOptionId(200L)
            .quantity(2)
            .build();

        List<CartItem> cartWithItem = new ArrayList<>();
        cartWithItem.add(itemToDelete);

        GuestCartItemListDto itemDto = GuestCartItemListDto.of(itemToDelete);
        List<GuestCartItemListDto> cartDtos = new ArrayList<>();
        cartDtos.add(itemDto);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(redisKey)).thenReturn(cartWithItem);
        when(objectMapper.convertValue(any(), Mockito.<TypeReference<List<CartItem>>>any()))
            .thenReturn(cartWithItem);

        // When
        guestCartService.deleteGuestCart(deleteDto);

        // Then
        verify(redisTemplate, times(2)).opsForValue();
        verify(valueOperations).get(redisKey);
        verify(valueOperations).set(eq(redisKey), argThat(list -> ((List<CartItem>)list).isEmpty()), eq(1L), eq(TimeUnit.DAYS));
    }

    @Test
    @DisplayName("비회원 장바구니 전체 삭제")
    void deleteAllGuestCart() {
        // When
        guestCartService.deleteAllGuestCart(sessionId);

        // Then
        verify(redisTemplate).delete(redisKey);
    }

    @Test
    @DisplayName("비회원 장바구니에 상품 추가 - 다른 상품 추가")
    void saveGuestCart_DifferentItem() {
        // Given
        GuestCartItemSaveDto saveDto = GuestCartItemSaveDto.builder()
            .sessionId(sessionId)
            .productId(300L)
            .productOptionId(400L)
            .quantity(1)
            .build();

        CartItem existingItem = CartItem.builder()
            .id(1L)
            .productId(100L)
            .productOptionId(200L)
            .quantity(3)
            .build();

        List<CartItem> cartWithExistingItem = new ArrayList<>();
        cartWithExistingItem.add(existingItem);

        GuestCartItemListDto existingItemDto = GuestCartItemListDto.of(existingItem);
        List<GuestCartItemListDto> cartDtos = new ArrayList<>();
        cartDtos.add(existingItemDto);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(redisKey)).thenReturn(cartWithExistingItem);
        when(objectMapper.convertValue(any(), Mockito.<TypeReference<List<CartItem>>>any()))
            .thenReturn(cartWithExistingItem);

        // When
        guestCartService.saveGuestCart(saveDto);

        // Then
        verify(redisTemplate, times(2)).opsForValue();
        verify(valueOperations).get(redisKey);
        verify(valueOperations).set(eq(redisKey), argThat(list -> ((List<CartItem>)list).size() == 2), eq(1L), eq(TimeUnit.DAYS));
    }
}
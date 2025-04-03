package com.commerce.cartservice.service;

import com.commerce.cartservice.dto.CartItemListDto;
import com.commerce.cartservice.dto.CartItemSaveDto;
import com.commerce.cartservice.dto.CartItemUpdateQuantityDto;
import com.commerce.cartservice.dto.ProductOptionDetailDto;
import com.commerce.cartservice.entity.Cart;
import com.commerce.cartservice.entity.CartItem;
import com.commerce.cartservice.feign.ProductServiceClient;
import com.commerce.cartservice.repository.CartItemRepository;
import com.commerce.cartservice.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @InjectMocks
    private CartService cartService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductServiceClient productServiceClient;

    @Mock
    private CacheManager cacheManager;

    private Cart cart;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        cart = Cart.builder()
            .id(1L)
            .userId("user1")
            .build();

        cartItem = CartItem.builder()
            .id(1L)
            .cart(cart)
            .productId(1L)
            .productOptionId(1L)
            .quantity(1)
            .build();
    }

    @Test
    @DisplayName("사용자별 장바구니 찾기 - 새로운 장바구니 생성")
    void findCartByUserId_NewCart() {
        // Given
        when(cartRepository.findByUserId("user1")).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        // When
        Cart result = cartService.findCartByUserId("user1");

        // Then
        assertNotNull(result);
        assertEquals("user1", result.getUserId());
        verify(cartRepository, times(1)).findByUserId("user1");
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    @DisplayName("장바구니 상품 등록 - 새로운 상품 추가")
    void saveCartItem_NewItem() {
        // Given
        List<CartItemSaveDto> cartItemSaveDtoList = List.of(
            new CartItemSaveDto(1L, 1L, 1)
        );

        when(cartRepository.findByUserId("user1")).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(cart.getId())).thenReturn(new ArrayList<>());
        when(cartItemRepository.saveAll(anyList())).thenReturn(List.of(cartItem));

        // When
        cartService.saveCartItem("user1", cartItemSaveDtoList);

        // Then
        verify(cartRepository, times(1)).findByUserId("user1");
        verify(cartItemRepository, times(1)).findByCartId(cart.getId());
        verify(cartItemRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("장바구니 상품 등록 - 기존 상품 수량 증가")
    void saveCartItem_ExistingItem() {
        // Given
        List<CartItemSaveDto> cartItemSaveDtoList = List.of(
            new CartItemSaveDto(1L, 1L, 1)
        );

        when(cartRepository.findByUserId("user1")).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(cart.getId())).thenReturn(List.of(cartItem));
        when(cartItemRepository.saveAll(anyList())).thenReturn(List.of(cartItem));

        // When
        cartService.saveCartItem("user1", cartItemSaveDtoList);

        // Then
        assertEquals(2, cartItem.getQuantity());
        verify(cartRepository, times(1)).findByUserId("user1");
        verify(cartItemRepository, times(1)).findByCartId(cart.getId());
        verify(cartItemRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("장바구니 상품 삭제")
    void removeCartItem() {
        // Given
        doNothing().when(cartItemRepository).deleteById(1L);

        // When
        cartService.removeCartItem("user1", 1L);

        // Then
        verify(cartItemRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("장바구니 상품 삭제 - 존재하지 않는 상품")
    void removeCartItem_NotFound() {
        // Given
        doThrow(new RuntimeException("장바구니 아이템을 찾을 수 없습니다.")).when(cartItemRepository).deleteById(999L);

        // When & Then
        assertThrows(RuntimeException.class, () -> cartService.removeCartItem("user1", 999L));
        verify(cartItemRepository, times(1)).deleteById(999L);
    }

    @Test
    @DisplayName("장바구니 상품 수량 변경")
    void updateCartItemQuantity() {
        // Given
        CartItemUpdateQuantityDto dto = new CartItemUpdateQuantityDto(1L, 2);
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem));

        // When
        cartService.updateCartItemQuantity(dto);

        // Then
        assertEquals(2, cartItem.getQuantity());
        verify(cartItemRepository, times(1)).findById(1L);
        verify(cartItemRepository, times(1)).save(cartItem);
    }

    @Test
    @DisplayName("장바구니 상품 수량 변경 - 존재하지 않는 상품")
    void updateCartItemQuantity_NotFound() {
        // Given
        CartItemUpdateQuantityDto dto = new CartItemUpdateQuantityDto(999L, 2);
        when(cartItemRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> cartService.updateCartItemQuantity(dto));
        verify(cartItemRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("장바구니 상품 목록 조회")
    void findCartItemList() {
        // Given
        ProductOptionDetailDto productOptionDetailDto = ProductOptionDetailDto.builder().build();
        cart.setCartItems(List.of(cartItem));

        when(cartRepository.findByUserId("user1")).thenReturn(Optional.of(cart));
        when(productServiceClient.findProductInfo(1L, 1L)).thenReturn(productOptionDetailDto);

        // When
        List<CartItemListDto> result = cartService.findCartItemList("user1");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(cartRepository, times(1)).findByUserId("user1");
        verify(productServiceClient, times(1)).findProductInfo(1L, 1L);
    }

    @Test
    @DisplayName("장바구니 상품 목록 조회 - 빈 장바구니")
    void findCartItemList_EmptyCart() {
        // Given
        cart.setCartItems(new ArrayList<>());
        when(cartRepository.findByUserId("user1")).thenReturn(Optional.of(cart));

        // When
        List<CartItemListDto> result = cartService.findCartItemList("user1");

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(cartRepository, times(1)).findByUserId("user1");
    }
}
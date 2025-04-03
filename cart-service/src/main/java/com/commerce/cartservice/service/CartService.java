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
import com.google.common.cache.Cache;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    private final ProductServiceClient productServiceClient;

    private final CacheManager cacheManager;

    /**
     * 사용자별 장바구니 찾기
     *
     * @param userId
     */
    public Cart findCartByUserId(String userId) {
        return cartRepository.findByUserId(userId)
            .orElseGet(() -> {
                Cart newCart = Cart.builder()
                    .userId(userId)
                    .build();
                return cartRepository.save(newCart);
            });
    }


    /**
     * 장바구니 상품 등록
     *
     * @param userId
     * @param cartItemSaveDtoList
     */
    @Transactional
    @CacheEvict(value = "cartCache", key = "#userId")
    public void saveCartItem(String userId, List<CartItemSaveDto> cartItemSaveDtoList) {
        Cart cart = findCartByUserId(userId);

        List<CartItem> existingCartItems = cartItemRepository.findByCartId(cart.getId()); // 기존 장바구니 상품 조회
        List<CartItem> cartItemSaveList = new ArrayList<>();

        for (CartItemSaveDto cartItemSaveDto : cartItemSaveDtoList) {
            Long productId = cartItemSaveDto.getProductId();
            Long productOptionId = cartItemSaveDto.getProductOptionId();
            int quantity = cartItemSaveDto.getQuantity();

            // 기존 아이템이 있는지 확인
            Optional<CartItem> existingItem = existingCartItems.stream()
                .filter(item -> item.getProductId().equals(productId) && item.getProductOptionId().equals(productOptionId))
                .findFirst();

            if (existingItem.isPresent()) {
                // 이미 존재하는 상품이면 수량 증가
                CartItem cartItem = existingItem.get();
                cartItem.setQuantity(cartItem.getQuantity() + quantity);
                cartItemSaveList.add(cartItem);
            } else {
                // 존재하지 않으면 새롭게 추가
                CartItem cartItem = cartItemSaveDto.toEntity();
                cartItem.setCart(cart);
                cartItemSaveList.add(cartItem);
            }
        }

        cartItemRepository.saveAll(cartItemSaveList); // 한 번에 저장
    }


    /**
     * 장바구니 상품 삭제
     *
     * @param cartItemId
     */
    @Transactional
    @CacheEvict(value = "cartCache", key = "#userId")
    public void removeCartItem(String userId, Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    /**
     * 장바구니 상품 수량 변경
     *
     * @param cartItemUpdateQuantityDto
     */
    @Transactional
    public void updateCartItemQuantity(CartItemUpdateQuantityDto cartItemUpdateQuantityDto) {
        CartItem cartItem = cartItemRepository.findById(cartItemUpdateQuantityDto.getCartItemId())
            .orElseThrow(() -> new RuntimeException("장바구니 아이템을 찾을 수 없습니다."));
        cartItem.setQuantity(cartItemUpdateQuantityDto.getQuantity());
        cartItemRepository.save(cartItem);
    }

    @Transactional
    @Cacheable(value = "cartCache", key = "#userId")
    public List<CartItemListDto> findCartItemList(String userId) {
        Cart cart = findCartByUserId(userId);

        List<CartItem> findCartItemList = cart.getCartItems();
        List<CartItemListDto> cartItemList = new ArrayList<CartItemListDto>();

        if (findCartItemList == null) {
            return new ArrayList<>();
        }

        for(CartItem cartItem : findCartItemList) {
            ProductOptionDetailDto dto = productServiceClient.findProductInfo(cartItem.getProductId(), cartItem.getProductOptionId());
            cartItemList.add(CartItemListDto.of(cartItem, dto));
        }

        return cartItemList;

    }




}

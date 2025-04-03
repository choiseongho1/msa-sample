package com.commerce.orderservice.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.commerce.common.dto.ResponseDto;
import com.commerce.common.exception.CustomException;
import com.commerce.orderservice.controller.OrderController;
import com.commerce.orderservice.dto.OrderListByUserDto;
import com.commerce.orderservice.dto.OrderProductListDto;
import com.commerce.orderservice.dto.OrderProductSaveDto;
import com.commerce.orderservice.dto.OrderSaveDto;
import com.commerce.orderservice.entity.Order;
import com.commerce.orderservice.service.OrderService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * OrderController 테스트 클래스
 *
 * @author : choiseongho
 * @fileName : OrderControllerTest.java
 * @since : 2025-03-13
 */
@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @Test
    @DisplayName("주문 저장 API 테스트 - 성공")
    void saveOrderTest_Success() {
        // given
        OrderSaveDto orderSaveDto = createOrderSaveDto();
        doNothing().when(orderService).processOrder(any(OrderSaveDto.class));

        // when
        ResponseDto<?> response = orderController.saveOrder(orderSaveDto);

        // then
        assertEquals(200, response.getCode());
        verify(orderService).processOrder(orderSaveDto);
    }

    @Test
    @DisplayName("주문 정보 조회 API 테스트 - 성공")
    void findOrderInfoTest_Success() {
        // given
        Long orderId = 1L;
        List<OrderProductListDto> orderProductList = createOrderProductList();

        given(orderService.findOrderProductList(anyLong())).willReturn(orderProductList);

        // when
        ResponseDto<?> response = orderController.findOrderInfo(orderId);

        // then
        assertEquals(200, response.getCode());
        assertEquals(orderProductList, response.getData());
        verify(orderService).findOrderProductList(orderId);
    }

    @Test
    @DisplayName("주문 정보 조회 API 테스트 - 실패 (주문 ID가 존재하지 않음)")
    void findOrderInfoTest_Fail_OrderNotFound() {
        // given
        Long orderId = 999L;

        given(orderService.findOrderProductList(anyLong()))
            .willThrow(new CustomException("주문을 찾을 수 없습니다."));

        // when & then
        CustomException exception = assertThrows(CustomException.class,
            () -> orderController.findOrderInfo(orderId));

        assertEquals("주문을 찾을 수 없습니다.", exception.getMessage());
        verify(orderService).findOrderProductList(orderId);
    }

    @Test
    @DisplayName("사용자별 주문 정보 조회 API 테스트 - 성공")
    void findOrderListByUserIdTest_Success() {
        // given
        String userId = "user123";
        Pageable pageable = PageRequest.of(0, 10);

        List<OrderListByUserDto> orderList = new ArrayList<>();
        OrderListByUserDto orderListByUserDto = createOrderListByUserDto();
        orderList.add(orderListByUserDto);

        Page<OrderListByUserDto> orderPage = new PageImpl<>(orderList, pageable, 1);

        given(orderService.findOrderListByUserId(anyString(), any(Pageable.class)))
            .willReturn(orderPage);

        // when
        ResponseDto<?> response = orderController.findOrderListByUserId(userId, pageable);

        // then
        assertEquals(200, response.getCode());
        assertEquals(orderPage, response.getData());
        verify(orderService).findOrderListByUserId(userId, pageable);
    }

    @Test
    @DisplayName("사용자별 주문 정보 조회 API 테스트 - 실패 (사용자 ID가 존재하지 않음)")
    void findOrderListByUserIdTest_Fail_UserNotFound() {
        // given
        String userId = "nonexistent";
        Pageable pageable = PageRequest.of(0, 10);

        given(orderService.findOrderListByUserId(anyString(), any(Pageable.class)))
            .willThrow(new CustomException("사용자를 찾을 수 없습니다."));

        // when & then
        CustomException exception = assertThrows(CustomException.class,
            () -> orderController.findOrderListByUserId(userId, pageable));

        assertEquals("사용자를 찾을 수 없습니다.", exception.getMessage());
        verify(orderService).findOrderListByUserId(userId, pageable);
    }

    @Test
    @DisplayName("Welcome 메시지 테스트")
    void welcomeTest() {
        // when
        String response = orderController.welcome();

        // then
        assertEquals("Welcome to the Order service.", response);
    }

    // 테스트 데이터 생성 헬퍼 메소드
    private OrderSaveDto createOrderSaveDto() {
        OrderSaveDto orderSaveDto = OrderSaveDto.builder()
            .userId("user123")
            .orderDate(LocalDateTime.now())
            .orderStatus(Order.OrderStatus.WAITING_FOR_PAYMENT)
            .orderShippingFee(3000)
            .orderPrice(13000)
            .orderCardCompany("신한카드")
            .orderUsername("홍길동")
            .orderZipcode("12345")
            .orderAddress("서울시 강남구")
            .orderPhone("01012345678")
            .orderReq("문 앞에 놓아주세요")
            .build();

        OrderProductSaveDto productDto = OrderProductSaveDto.builder()
            .productId(1L)
            .orderProductQuantity(2)
            .orderProductPrice(5000)
            .orderProductOption(1L)
            .orderProductOptionPrice(0)
            .build();

        orderSaveDto.getOrderProductSaveList().add(productDto);

        return orderSaveDto;
    }

    private List<OrderProductListDto> createOrderProductList() {
        List<OrderProductListDto> orderProductList = new ArrayList<>();

        OrderProductListDto orderProductListDto = OrderProductListDto.builder()
            .productId(1L)
            .orderProductQuantity(2)
            .orderProductPrice(5000)
            .orderProductOption(1L)
            .orderProductOptionPrice(0)
            .imageLob("이미지데이터")
            .build();

        orderProductList.add(orderProductListDto);

        return orderProductList;
    }

    private OrderListByUserDto createOrderListByUserDto() {
        return OrderListByUserDto.builder()
            .orderId(1L)
            .orderDate(LocalDateTime.now())
            .orderStatus(Order.OrderStatus.WAITING_FOR_PAYMENT)
            .orderShippingFee(3000)
            .orderPrice(13000)
            .orderCardCompany("신한카드")
            .orderProductList(createOrderProductList())
            .build();
    }
}
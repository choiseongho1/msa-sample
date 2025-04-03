package com.commerce.orderservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.commerce.common.exception.CustomException;
import com.commerce.orderservice.dto.OrderListByUserDto;
import com.commerce.orderservice.dto.OrderProductListDto;
import com.commerce.orderservice.dto.OrderProductSaveDto;
import com.commerce.orderservice.dto.OrderSaveDto;
import com.commerce.orderservice.dto.ProductOptionDto;
import com.commerce.orderservice.dto.ProductPriceDto;
import com.commerce.orderservice.entity.Order;
import com.commerce.orderservice.entity.OrderProduct;
import com.commerce.orderservice.feign.ImageServiceClient;
import com.commerce.orderservice.feign.ProductServiceClient;
import com.commerce.orderservice.feign.UserServiceClient;
import com.commerce.orderservice.kafka.OrderEventProducer;
import com.commerce.orderservice.kafka.ProductStockProducer;
import com.commerce.orderservice.repository.OrderProductRepository;
import com.commerce.orderservice.repository.OrderRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
 * OrderService 테스트 클래스
 *
 * @author : choiseongho
 * @fileName : OrderServiceTest.java
 * @since : 2025-03-13
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderProductRepository orderProductRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private ProductServiceClient productServiceClient;

    @Mock
    private ImageServiceClient imageServiceClient;

    @Mock
    private ProductStockProducer productStockProducer;

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderEventProducer orderEventProducer;

    @Test
    @DisplayName("주문 저장 테스트 - 성공")
    void saveOrderTest_Success() {
        // given
        OrderSaveDto orderSaveDto = createOrderSaveDto();
        Order order = orderSaveDto.toEntity();

        given(productServiceClient.validateProduct(anyLong())).willReturn(true);
        given(productServiceClient.getProductPrice(anyLong())).willReturn(
            ProductPriceDto.builder().price(5000).build());
        given(productServiceClient.getProductOption(anyLong(), anyLong())).willReturn(
            ProductOptionDto.builder().price(0).build());
        given(orderRepository.save(any(Order.class))).willReturn(order);
        given(orderProductRepository.saveAll(anyList())).willReturn(new ArrayList<>());

        // when
        Order savedOrder = orderService.saveOrder(orderSaveDto);

        // then
        assertNotNull(savedOrder);
        assertEquals(orderSaveDto.getUserId(), savedOrder.getUserId());
        assertEquals(orderSaveDto.getOrderPrice(), savedOrder.getOrderPrice());
        verify(productServiceClient, times(1)).validateProduct(anyLong());
        verify(productServiceClient, times(1)).getProductPrice(anyLong());
        verify(productServiceClient, times(1)).getProductOption(anyLong(), anyLong());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderProductRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("주문 저장 테스트 - 실패 (유효하지 않은 상품)")
    void saveOrderTest_Fail_InvalidProduct() {
        // given
        OrderSaveDto orderSaveDto = createOrderSaveDto();
        given(productServiceClient.validateProduct(anyLong())).willReturn(false);

        // when & then
        CustomException exception = assertThrows(CustomException.class,
            () -> orderService.saveOrder(orderSaveDto));

        assertEquals("유효하지 않은 상품입니다.", exception.getMessage());
        verify(productServiceClient, times(1)).validateProduct(anyLong());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("주문 저장 테스트 - 실패 (상품 가격 불일치)")
    void saveOrderTest_Fail_PriceMismatch() {
        // given
        OrderSaveDto orderSaveDto = createOrderSaveDto();
        given(productServiceClient.validateProduct(anyLong())).willReturn(true);
        given(productServiceClient.getProductPrice(anyLong())).willReturn(
            ProductPriceDto.builder().price(6000).build()); // 다른 가격

        // when & then
        CustomException exception = assertThrows(CustomException.class,
            () -> orderService.saveOrder(orderSaveDto));

        assertEquals("상품 가격이 일치하지 않습니다.", exception.getMessage());
        verify(productServiceClient, times(1)).validateProduct(anyLong());
        verify(productServiceClient, times(1)).getProductPrice(anyLong());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("주문 저장 테스트 - 실패 (상품 옵션 가격 불일치)")
    void saveOrderTest_Fail_OptionPriceMismatch() {
        // given
        OrderSaveDto orderSaveDto = createOrderSaveDto();
        given(productServiceClient.validateProduct(anyLong())).willReturn(true);
        given(productServiceClient.getProductPrice(anyLong())).willReturn(
            ProductPriceDto.builder().price(5000).build());
        given(productServiceClient.getProductOption(anyLong(), anyLong())).willReturn(
            ProductOptionDto.builder().price(1000).build()); // 다른 옵션 가격

        // when & then
        CustomException exception = assertThrows(CustomException.class,
            () -> orderService.saveOrder(orderSaveDto));

        assertEquals("상품 옵션 가격이 일치하지 않습니다.", exception.getMessage());
        verify(productServiceClient, times(1)).validateProduct(anyLong());
        verify(productServiceClient, times(1)).getProductPrice(anyLong());
        verify(productServiceClient, times(1)).getProductOption(anyLong(), anyLong());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("주문 처리 테스트 - 성공")
    void processOrderTest_Success() {
        // given
        OrderSaveDto orderSaveDto = createOrderSaveDto();
        Order order = orderSaveDto.toEntity();
        order.setId(1L);

        given(userServiceClient.validateUser(anyString())).willReturn(true);
        given(productServiceClient.validateProduct(anyLong())).willReturn(true);
        given(productServiceClient.getProductPrice(anyLong())).willReturn(
            ProductPriceDto.builder().price(5000).build());
        given(productServiceClient.getProductOption(anyLong(), anyLong())).willReturn(
            ProductOptionDto.builder().price(0).build());
        given(orderRepository.save(any(Order.class))).willReturn(order);
        given(orderProductRepository.saveAll(anyList())).willReturn(new ArrayList<>());
        doNothing().when(productStockProducer).decreaseStock(anyLong(), anyLong(), anyInt(), anyLong());
        doNothing().when(orderEventProducer).publishOrderCreatedEvent(anyLong(), anyString(), anyInt());

        // when
        orderService.processOrder(orderSaveDto);

        // then
        verify(userServiceClient, times(1)).validateUser(anyString());
        verify(productServiceClient, times(1)).validateProduct(anyLong());
        verify(productServiceClient, times(1)).getProductPrice(anyLong());
        verify(productServiceClient, times(1)).getProductOption(anyLong(), anyLong());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderProductRepository, times(1)).saveAll(anyList());
        verify(productStockProducer, times(1)).decreaseStock(anyLong(), anyLong(), anyInt(), anyLong());
        verify(orderEventProducer, times(1)).publishOrderCreatedEvent(anyLong(), anyString(), anyInt());
    }

    @Test
    @DisplayName("주문 처리 테스트 - 실패 (유효하지 않은 사용자)")
    void processOrderTest_Fail_InvalidUser() {
        // given
        OrderSaveDto orderSaveDto = createOrderSaveDto();
        given(userServiceClient.validateUser(anyString())).willReturn(false);

        // when & then
        CustomException exception = assertThrows(CustomException.class,
            () -> orderService.processOrder(orderSaveDto));

        assertEquals("유효하지 않은 사용자입니다.", exception.getMessage());
        verify(userServiceClient, times(1)).validateUser(anyString());
        verify(orderRepository, never()).save(any(Order.class));
        verify(productStockProducer, never()).decreaseStock(anyLong(), anyLong(), anyInt(), anyLong());
    }

    @Test
    @DisplayName("사용자별 주문 정보 조회 테스트 - 성공")
    void findOrderListByUserIdTest_Success() {
        // given
        String userId = "user123";
        Pageable pageable = PageRequest.of(0, 10);

        List<OrderListByUserDto> orderList = new ArrayList<>();
        OrderListByUserDto orderListByUserDto = createOrderListByUserDto();
        orderList.add(orderListByUserDto);

        Page<OrderListByUserDto> orderPage = new PageImpl<>(orderList, pageable, 1);

        given(orderRepository.findOrderListByUser(anyString(), any(Pageable.class)))
            .willReturn(orderPage);

        // when
        Page<OrderListByUserDto> result = orderService.findOrderListByUserId(userId, pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(orderListByUserDto.getOrderId(), result.getContent().get(0).getOrderId());
        verify(orderRepository, times(1)).findOrderListByUser(userId, pageable);
    }
    @Test
    @DisplayName("주문 정보별 주문 상품 목록 조회 테스트 - 성공 (이미지 있음)")
    void findOrderProductListTest_Success_WithImage() {
        // given
        Long orderId = 1L;
        List<OrderProductListDto> orderProductList = createOrderProductList();

        given(orderRepository.findOrderProductList(anyLong())).willReturn(orderProductList);
        given(productServiceClient.findProductImageId(anyLong())).willReturn(1L);
        given(imageServiceClient.getImageLob(anyLong())).willReturn("이미지데이터");

        // when
        List<OrderProductListDto> result = orderService.findOrderProductList(orderId);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("이미지데이터", result.get(0).getImageLob());
        verify(orderRepository, times(1)).findOrderProductList(orderId);
        verify(productServiceClient, times(1)).findProductImageId(anyLong());
        verify(imageServiceClient, times(1)).getImageLob(anyLong());
    }

    @Test
    @DisplayName("주문 정보별 주문 상품 목록 조회 테스트 - 성공 (이미지 없음)")
    void findOrderProductListTest_Success_WithoutImage() {
        // given
        Long orderId = 1L;
        List<OrderProductListDto> orderProductList = createOrderProductList();

        given(orderRepository.findOrderProductList(anyLong())).willReturn(orderProductList);
        given(productServiceClient.findProductImageId(anyLong())).willReturn(null);

        // when
        List<OrderProductListDto> result = orderService.findOrderProductList(orderId);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository, times(1)).findOrderProductList(orderId);
        verify(productServiceClient, times(1)).findProductImageId(anyLong());
        verify(imageServiceClient, never()).getImageLob(anyLong());
    }

    @Test
    @DisplayName("주문 정보별 주문 상품 목록 조회 테스트 - 실패 (이미지 서비스 오류)")
    void findOrderProductListTest_Fail_ImageServiceError() {
        // given
        Long orderId = 1L;
        List<OrderProductListDto> orderProductList = createOrderProductList();

        given(orderRepository.findOrderProductList(anyLong())).willReturn(orderProductList);
        given(productServiceClient.findProductImageId(anyLong())).willReturn(1L);
        given(imageServiceClient.getImageLob(anyLong())).willThrow(new RuntimeException("이미지 서비스 오류"));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> orderService.findOrderProductList(orderId));

        assertEquals("이미지 서비스 오류", exception.getMessage());
        verify(orderRepository, times(1)).findOrderProductList(orderId);
        verify(productServiceClient, times(1)).findProductImageId(anyLong());
        verify(imageServiceClient, times(1)).getImageLob(anyLong());
    }

    @Test
    @DisplayName("주문 상태 업데이트 테스트 - 성공")
    void updateOrderStatusTest_Success() {
        // given
        Long orderId = 1L;
        Order order = Order.builder()
            .id(orderId)
            .orderStatus(Order.OrderStatus.WAITING_FOR_PAYMENT)
            .build();

        given(orderRepository.findById(anyLong())).willReturn(Optional.of(order));
        given(orderRepository.save(any(Order.class))).willReturn(order);

        // when
        orderService.updateOrderStatus(orderId, Order.OrderStatus.SHIPPING);

        // then
        assertEquals(Order.OrderStatus.SHIPPING, order.getOrderStatus());
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    @DisplayName("주문 상태 업데이트 테스트 - 실패 (주문 없음)")
    void updateOrderStatusTest_Fail_OrderNotFound() {
        // given
        Long orderId = 999L;
        given(orderRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class,
            () -> orderService.updateOrderStatus(orderId, Order.OrderStatus.SHIPPING));

        assertEquals("주문을 찾을 수 없습니다.", exception.getMessage());
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Fallback 메소드 테스트")
    void fallbackOrderProductListTest() {
        // given
        Long orderId = 1L;
        List<OrderProductListDto> orderProductList = createOrderProductList();
        Throwable throwable = new RuntimeException("이미지 서비스 오류");

        given(orderRepository.findOrderProductList(anyLong())).willReturn(orderProductList);

        // when
        List<OrderProductListDto> result = orderService.fallbackOrderProductList(orderId, throwable);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).getImageLob());
        verify(orderRepository, times(1)).findOrderProductList(orderId);
    }

    @Test
    @DisplayName("주문 저장 테스트 - 성공 (여러 상품 주문)")
    void saveOrderTest_Success_MultipleProducts() {
        // given
        OrderSaveDto orderSaveDto = createOrderSaveDtoWithMultipleProducts();
        Order order = orderSaveDto.toEntity();

        given(productServiceClient.validateProduct(anyLong())).willReturn(true);
        given(productServiceClient.getProductPrice(anyLong())).willReturn(
            ProductPriceDto.builder().price(5000).build());
        given(productServiceClient.getProductOption(anyLong(), anyLong())).willReturn(
            ProductOptionDto.builder().price(0).build());
        given(orderRepository.save(any(Order.class))).willReturn(order);
        given(orderProductRepository.saveAll(anyList())).willReturn(new ArrayList<>());

        // when
        Order savedOrder = orderService.saveOrder(orderSaveDto);

        // then
        assertNotNull(savedOrder);
        assertEquals(orderSaveDto.getUserId(), savedOrder.getUserId());
        assertEquals(orderSaveDto.getOrderPrice(), savedOrder.getOrderPrice());
        verify(productServiceClient, times(2)).validateProduct(anyLong());
        verify(productServiceClient, times(2)).getProductPrice(anyLong());
        verify(productServiceClient, times(2)).getProductOption(anyLong(), anyLong());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderProductRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("주문 저장 테스트 - 성공 (옵션 없는 상품)")
    void saveOrderTest_Success_NoOption() {
        // given
        OrderSaveDto orderSaveDto = createOrderSaveDtoWithNoOption();
        Order order = orderSaveDto.toEntity();

        given(productServiceClient.validateProduct(anyLong())).willReturn(true);
        given(productServiceClient.getProductPrice(anyLong())).willReturn(
            ProductPriceDto.builder().price(5000).build());
        given(orderRepository.save(any(Order.class))).willReturn(order);
        given(orderProductRepository.saveAll(anyList())).willReturn(new ArrayList<>());

        // when
        Order savedOrder = orderService.saveOrder(orderSaveDto);

        // then
        assertNotNull(savedOrder);
        assertEquals(orderSaveDto.getUserId(), savedOrder.getUserId());
        assertEquals(orderSaveDto.getOrderPrice(), savedOrder.getOrderPrice());
        verify(productServiceClient, times(1)).validateProduct(anyLong());
        verify(productServiceClient, times(1)).getProductPrice(anyLong());
        verify(productServiceClient, never()).getProductOption(anyLong(), anyLong());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderProductRepository, times(1)).saveAll(anyList());
    }

//    @Test
//    @DisplayName("주문 취소 테스트 - 성공")
//    void cancelOrderTest_Success() {
//        // given
//        Long orderId = 1L;
//        Order order = Order.builder()
//            .id(orderId)
//            .orderStatus(Order.OrderStatus.PAYMENT_COMPLETED) // PAYMENT_COMPLETED 상태로 설정
//            .build();
//
//        List<OrderProduct> orderProducts = new ArrayList<>();
//        OrderProduct orderProduct = OrderProduct.builder()
//            .productId(1L)
//            .orderProductOption(1L)
//            .orderProductQuantity(2)
//            .build();
//        orderProducts.add(orderProduct);
//
//        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
//        given(orderProductRepository.findByOrderId(orderId)).willReturn(orderProducts);
//        given(orderRepository.save(order)).willReturn(order);
//        doNothing().when(orderEventProducer).publishOrderCancelledEvent(orderId);
//        doNothing().when(productStockProducer).increaseStock(
//            orderProduct.getProductId(),
//            orderProduct.getOrderProductOption(),
//            orderProduct.getOrderProductQuantity(),
//            orderId
//        );
//
//        // when
//        Order cancelledOrder = orderService.cancelOrder(orderId);
//
//        // then
//        assertNotNull(cancelledOrder);
//        assertEquals(Order.OrderStatus.CANCELED, cancelledOrder.getOrderStatus());
//        verify(orderRepository, times(1)).findById(orderId);
//        verify(orderProductRepository, times(1)).findByOrderId(orderId);
//        verify(orderEventProducer, times(1)).publishOrderCancelledEvent(orderId); // 호출 검증
//        verify(productStockProducer, times(1)).increaseStock(
//            orderProduct.getProductId(),
//            orderProduct.getOrderProductOption(),
//            orderProduct.getOrderProductQuantity(),
//            orderId
//        );
//        verify(orderRepository, times(1)).save(order);
//    }

    @Test
    @DisplayName("주문 취소 테스트 - 실패 (주문 없음)")
    void cancelOrderTest_Fail_OrderNotFound() {
        // given
        Long orderId = 999L;
        given(orderRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class,
            () -> orderService.cancelOrder(orderId));

        assertEquals("주문을 찾을 수 없습니다.", exception.getMessage());
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
        verify(productStockProducer, never()).increaseStock(anyLong(), anyLong(), anyInt(), anyLong());
    }

    @Test
    @DisplayName("주문 취소 테스트 - 실패 (취소 불가능한 상태)")
    void cancelOrderTest_Fail_InvalidStatus() {
        // given
        Long orderId = 1L;
        Order order = Order.builder()
            .id(orderId)
            .orderStatus(Order.OrderStatus.SHIPPING)
            .build();

        given(orderRepository.findById(anyLong())).willReturn(Optional.of(order));

        // when & then
        CustomException exception = assertThrows(CustomException.class,
            () -> orderService.cancelOrder(orderId));

        assertEquals("취소할 수 없는 주문 상태입니다: " + Order.OrderStatus.SHIPPING, exception.getMessage());
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
        verify(productStockProducer, never()).increaseStock(anyLong(), anyLong(), anyInt(), anyLong());
    }



    // 추가 헬퍼 메소드
    private OrderSaveDto createOrderSaveDtoWithMultipleProducts() {
        OrderSaveDto orderSaveDto = OrderSaveDto.builder()
            .userId("user123")
            .orderDate(LocalDateTime.now())
            .orderStatus(Order.OrderStatus.WAITING_FOR_PAYMENT)
            .orderShippingFee(3000)
            .orderPrice(23000)
            .orderCardCompany("신한카드")
            .orderUsername("홍길동")
            .orderZipcode("12345")
            .orderAddress("서울시 강남구")
            .orderPhone("01012345678")
            .orderReq("문 앞에 놓아주세요")
            .build();

        // 첫 번째 상품
        OrderProductSaveDto productDto1 = OrderProductSaveDto.builder()
            .productId(1L)
            .orderProductQuantity(2)
            .orderProductPrice(5000)
            .orderProductOption(1L)
            .orderProductOptionPrice(0)
            .build();

        // 두 번째 상품
        OrderProductSaveDto productDto2 = OrderProductSaveDto.builder()
            .productId(2L)
            .orderProductQuantity(1)
            .orderProductPrice(10000)
            .orderProductOption(2L)
            .orderProductOptionPrice(0)
            .build();

        orderSaveDto.getOrderProductSaveList().add(productDto1);
        orderSaveDto.getOrderProductSaveList().add(productDto2);

        return orderSaveDto;
    }

    private OrderSaveDto createOrderSaveDtoWithNoOption() {
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
            .orderProductOption(null)  // 옵션 없음
            .orderProductOptionPrice(null)  // 옵션 가격 없음
            .build();

        orderSaveDto.getOrderProductSaveList().add(productDto);

        return orderSaveDto;
    }


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

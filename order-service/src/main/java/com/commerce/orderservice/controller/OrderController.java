package com.commerce.orderservice.controller;

import com.commerce.common.dto.ResponseDto;
import com.commerce.orderservice.dto.OrderListByUserDto;
import com.commerce.orderservice.dto.OrderProductListDto;
import com.commerce.orderservice.dto.OrderSaveDto;
import com.commerce.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * order-service의 Controller
 *
 * @author : choiseongho
 * @fileName : OrderController.java
 * @since : 2025-01-09
 */

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    /**
     * 주문내역 저장
     *
     * @param orderSaveDto the order save dto
     */
    @PostMapping
    @Operation(summary = "주문 내역 저장")
    public ResponseDto<?> saveOrder(@RequestBody OrderSaveDto orderSaveDto) {
        orderService.processOrder(orderSaveDto);
        return ResponseDto.ok();
    }

    /**
     * 주문 취소
     *
     * @param orderId 주문 ID
     */
    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "주문 취소")
    public ResponseDto<?> cancelOrder(@PathVariable(name = "orderId") Long orderId) {
        orderService.cancelOrder(orderId);
        return ResponseDto.ok();
    }

    /**
     * 주문정보 별 주문 상품 조회
     *
     * @param orderId the order id
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "주문 정보 별 주문 상품 조회")
    public ResponseDto<?> findOrderInfo(@PathVariable(name = "orderId") Long orderId) {
        List<OrderProductListDto> orderProductList = orderService.findOrderProductList(orderId);
        return ResponseDto.ok(orderProductList);
    }

    /**
     * 사용자 별 주문정보 조회
     *
     * @param userId   the user id
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "사용자 별 주문 정보 조회")
    public ResponseDto<?> findOrderListByUserId(@PathVariable(name = "userId") String userId, @PageableDefault(page = 0, size = 10) Pageable pageable) {
        Page<OrderListByUserDto> orderList = orderService.findOrderListByUserId(userId, pageable);
        return ResponseDto.ok(orderList);
    }

}
